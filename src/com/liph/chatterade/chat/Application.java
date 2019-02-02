package com.liph.chatterade.chat;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.liph.chatterade.chat.enums.MessageProcessMap;
import com.liph.chatterade.chat.models.Channel;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.Server;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.common.Pair;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.connection.ConnectionListener;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.connection.models.RecentMessage;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.messaging.models.*;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import com.liph.chatterade.parsing.models.Target;
import java.net.Socket;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class Application {

    private static final int RECENT_MESSAGE_SET_MAX_SIZE = 10000;
    private static final int RECENT_MESSAGE_MAX_AGE_MS = 1000 * 60;

    private final Instant startupTime;
    private final String serverName;
    private final String serverVersion;
    private final EncryptionService encryptionService;
    private final ConnectionListener clientListener;
    private final ConnectionListener serverListener;
    private final Set<ServerConnection> serverConnections;

    // TODO: keyed hash code to prevent DoS?
    private final Map<ByteArray, ClientUser> clientUsersByPublicKey;

    private final Map<ByteArray, RecentMessage> recentMessageSet;
    private final Queue<RecentMessage> recentMessageQueue;


    public Application(String serverName, String serverVersion, int clientPort, int serverPort) {
        this.startupTime = Instant.now();
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.encryptionService = new EncryptionService();
        this.clientListener = new ConnectionListener(this, clientPort, ClientConnection::new);
        this.serverListener = new ConnectionListener(this, serverPort, ServerConnection::new);
        this.serverConnections = ConcurrentHashMap.newKeySet();
        this.clientUsersByPublicKey = new ConcurrentHashMap<>();
        this.recentMessageSet = new HashMap<>();
        this.recentMessageQueue = new LinkedList<>();
    }

    public void run() {
        verifyCodeConsistency();

        new Thread(this.serverListener).start();
        this.clientListener.run();
    }


    public String getServerName() {
        return serverName;
    }


    public void removeServer(ServerConnection server) {
        serverConnections.remove(server);
    }


    public ClientUser addUser(ClientUser user) {
        Key key = encryptionService.generateKey();
        user.setKey(Optional.of(key));

        clientUsersByPublicKey.put(key.getSigningPublicKey(), user);

        sendWelcomeMessage(user.getConnection(), key.getBase64SigningPublicKey(), user);
        return user;
    }

    public void removeUser(ClientUser clientUser) {
        /*
        Set<Channel> channels = clientUser.getChannels();
        for(Channel channel : channels) {
            channel.getUsers().remove(clientUser);
        }
        */

        clientUser.getKey().ifPresent(k -> clientUsersByPublicKey.remove(k.getSigningPublicKey()));
    }
    
    
    public void processJoin(JoinMessage message) {
    /*    ClientUser user = message.getSender();

        for(Entry<String, Optional<String>> channelKey : message.getChannelKeyMap().entrySet()) {
            Channel channel = new Channel(channelKey.getKey(), channelKey.getValue());
            if(!channels.add(channel))
                channel = channels;

        }
        */
    }

    public void processNick(NickMessage message) {
        message.getSender().setNick(Optional.of(message.getNewNick()));
    }

    public void processNotice(NoticeMessage message) {

    }

    public void processPart(PartMessage message) {

    }

    public void processPass(PassMessage message) {

    }

    public void processPrivateMessage(PrivateMessage message) {
        /*
        Pair<Optional<ClientUser>, Optional<String>> targetAndPreviousNick = resolveTargetClientUser(message.getTarget(), message.getSender());
        Optional<ClientUser> target = targetAndPreviousNick.getFirst();
        Optional<String> previousNick = targetAndPreviousNick.getSecond();
        */
        Optional<ClientUser> senderClientUser = Optional.empty();

        if(message.getSender() instanceof ClientUser) {
           senderClientUser = Optional.of((ClientUser)message.getSender());
        }

        Optional<User> targetOpt = resolveTargetUser(message.getTarget(), senderClientUser);

        if(targetOpt.isPresent()) {
            User target = targetOpt.get();
            
            //previousNick.ifPresent(n -> message.getSender().getConnection().sendMessage(format(":%s NICK %s", n, target.get().getNick().get())));
            String targetNick = target.getNick().orElse(target.getKey().get().getBase64SigningPublicKey());

            if(!targetNick.equals(message.getTargetText())) {
                senderClientUser.ifPresent(u -> sendNickChange(u, message.getTargetText(), target));
            }

            if(target instanceof ClientUser) {
                ClientUser targetClientUser = (ClientUser)target;
                Optional<String> previousNick = targetClientUser.addOrUpdateContact(message.getSender());
                previousNick.ifPresent(previous -> sendNickChange(targetClientUser, previous, message.getSender()));

                targetClientUser.getConnection().sendMessage(message.getSender().getFullyQualifiedName(), MessageType.PRIVMSG.getIrcCommand(), format(":%s", message.getText()));
            } else {
                senderClientUser.ifPresent(u -> sendNetworkMessage(u, MessageType.PRIVMSG, target, format(":%s", message.getText())));
            }
        } else {
            senderClientUser.ifPresent(u -> u.getConnection().sendMessage(serverName, "401", format("%s :No such nick/channel", message.getTargetText())));
        }
    }

    public void processQuit(QuitMessage message) {

    }

    public void processUser(UserMessage message) {

    }

    public void processPing(PingMessage message) {
        ((ClientUser)message.getSender()).getConnection().sendMessage(serverName, MessageType.PONG.getIrcCommand(), message.getText());
    }

    public void processPong(PongMessage message) {

    }

    public void processConnect(ConnectMessage message) {
        try {
            Socket socket = new Socket(message.getServer(), message.getPort().orElse(6667));
            ServerConnection connection = new ServerConnection(this, socket);
            new Thread(connection).start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void addServerConnection(ServerConnection server) {
        serverConnections.add(server);
        System.out.println("Server added.");
    }


    public boolean relayMessage(String message, Optional<ServerConnection> originator) {
        RecentMessage recentMessage = new RecentMessage(encryptionService.getMessageHash(message));

        if(!addToRecentMessages(recentMessage))
            return false;

        for(ServerConnection connection : serverConnections) {
            if(!originator.isPresent() || connection != originator.get())
                connection.sendMessage(message);
        }

        return true;
    }


    private boolean addToRecentMessages(RecentMessage message) {
        synchronized (recentMessageSet) {
            while(recentMessageQueue.size() > RECENT_MESSAGE_SET_MAX_SIZE || (!recentMessageQueue.isEmpty() && recentMessageQueue.peek().getAgeMs() > RECENT_MESSAGE_MAX_AGE_MS)) {
                RecentMessage evicted = recentMessageQueue.remove();
                recentMessageSet.remove(evicted.getHash());
            }

            if(recentMessageSet.containsKey(message.getHash()))
                return false;

            recentMessageSet.put(message.getHash(), message);
            recentMessageQueue.add(message);
            return true;
        }
    }


    private void sendNetworkMessage(User sender, MessageType messageType, User target, String arguments) {
        String targetPublicKey = target.getKey().get().getBase64SigningPublicKey();
        String message = format(":%s %s %s %s", sender.getFullyQualifiedName(), messageType.getIrcCommand(), targetPublicKey, arguments);
        // TODO: relay the *encrypted* message
        relayMessage(message, Optional.empty());
    }

    private void sendNickChange(ClientUser user, String previousNick, User contact) {
        String publicKey = contact.getKey().map(Key::getBase64SigningPublicKey).orElse("unknown");
        user.getConnection().sendMessage(format(":%s!%s@%s NICK %s", previousNick, contact.getUsername().orElse("unknown"), publicKey, contact.getNick().get()));
    }

    private Optional<User> resolveTargetUser(Target target, Optional<ClientUser> sender) {
        if(target.getTargetType() != TargetType.USER)   // TODO: error?
            return Optional.empty(); //Pair.of(Optional.empty(), Optional.empty());

        Optional<User> targetRemoteUser = Optional.empty();
        Optional<Contact> targetContact = Optional.empty();

        if(target.getPublicKey().isPresent())
            targetRemoteUser = Optional.of(new User(target.getNick(), Optional.empty(), target.getPublicKey().map(Key::new)));

        Optional<ClientUser> targetClientUser = target.getPublicKeyBytes().flatMap(this::getClientUserByPublicKey);
        System.out.println(format("%s isPresent=%b", target.getPublicKey().orElse("none"), targetClientUser.isPresent()));

        if(sender.isPresent()) {
            if (!targetClientUser.isPresent() && target.getNick().isPresent()) {
                targetContact = sender.get().getContactByNick(target.getNick().get());
                if (targetContact.isPresent()) {
                    targetClientUser = getClientUserByContact(targetContact.get());
                }
            }

            if(targetClientUser.isPresent())
                sender.get().addOrUpdateContact(targetClientUser.get());
            else if(targetRemoteUser.isPresent() && targetRemoteUser.get().getNick().isPresent())
                sender.get().addOrUpdateContact(targetRemoteUser.get());
        }

        if(targetClientUser.isPresent())
            return targetClientUser.map(u -> (User)u);
        else if(targetContact.isPresent() && targetContact.get().getKey().isPresent())
            return targetContact.map(u -> (User)u);
        else
            return targetRemoteUser;
        //Optional<String> previousNick = targetClientUser.flatMap(c -> sender.addOrUpdateContact(c.getNick(), c.getKey()));
        //return Pair.of(targetClientUser, previousNick);
    }

    private Optional<ClientUser> getClientUserByPublicKey(ByteArray publicKey) {
        return Optional.ofNullable(clientUsersByPublicKey.get(publicKey));
    }

    private Optional<ClientUser> getClientUserByPublicKey(Key key) {
        return getClientUserByPublicKey(key.getSigningPublicKey());
    }

    private Optional<ClientUser> getClientUserByContact(User user) {
        return user.getKey().flatMap(this::getClientUserByPublicKey);
    }


    private void sendWelcomeMessage(ClientConnection connection, String keyBase64, ClientUser user) {
        connection.sendMessage(serverName, "001", format(":Welcome to the Internet Relay Network %s", user.getFullyQualifiedName()));
        connection.sendMessage(serverName, "002", format(":Your host is %s, running version %s", serverName, serverVersion));
        connection.sendMessage(serverName, "003", format(":This server was created %s", startupTime));
        connection.sendMessage(serverName, "004", format("%s %s DOQRSZaghilopswz CFILMPQSbcefgijklmnopqrstvz bkloveqjfI", serverName, serverVersion));
        connection.sendMessage(serverName, "375", format(":- %s Message of the Day -", serverName));
        connection.sendMessage(serverName, "372", "Welcome to my test chatterade server!");
        connection.sendMessage(serverName, "376", ":End of /MOTD command.");
        connection.sendMessage(serverName, "NOTICE", format(":Your public key is %s. Users need to /msg %s@%s to message you.", keyBase64, user.getNick().get(), keyBase64));
    }

    private void verifyCodeConsistency() {
        verifyEnumConsistency(MessageProcessMap.class, MessageProcessMap.values());
        verifyEnumConsistency(IrcMessageValidationMap.class, IrcMessageValidationMap.values());
    }


    private <T extends Enum<T>> void verifyEnumConsistency(Class<T> type, T[] values) {
        List<T> list = Stream.of(values).collect(toList());

        for(MessageType messageType : MessageType.values()) {
            T process = EnumHelper.fromName(values, messageType.name())
                .orElseThrow(() -> new IllegalStateException(format("%s is missing MessageType %s", type.getSimpleName(), messageType.name())));
            list.remove(process);
        }

        if(!list.isEmpty())
            throw new IllegalStateException(format("%s contains extra entries not in MessageType: %s", type.getSimpleName(),
                String.join(", ", list.stream().map(Enum::name).collect(toList()))));
    }
}
