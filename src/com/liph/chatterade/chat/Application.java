package com.liph.chatterade.chat;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.liph.chatterade.chat.enums.MessageProcessMap;
import com.liph.chatterade.chat.models.Channel;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.common.Pair;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.connection.ConnectionListener;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.messaging.models.*;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import com.liph.chatterade.parsing.models.Target;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class Application {

    private final Instant startupTime;
    private final String serverName;
    private final String serverVersion;
    private final EncryptionService encryptionService;
    private final ConnectionListener clientListener;
    private final ConnectionListener serverListener;

    private final Map<String, ClientUser> clientUsersByPublicKey;


    public Application(String serverName, String serverVersion, int clientPort, int serverPort) {
        this.startupTime = Instant.now();
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.encryptionService = new EncryptionService();
        this.clientListener = new ConnectionListener(this, clientPort, ClientConnection::new);
        this.serverListener = new ConnectionListener(this, serverPort, ServerConnection::new);
        this.clientUsersByPublicKey = new ConcurrentHashMap<>();
    }

    public void run() {
        verifyCodeConsistency();

        new Thread(this.serverListener).start();
        this.clientListener.run();
    }


    public String getServerName() {
        return serverName;
    }


    public ClientUser addUser(ClientUser user) {
        Key key = encryptionService.generateKey();
        user.setKey(Optional.of(key));

        String keyBase64 = key.getBase64SigningPublicKey();

        clientUsersByPublicKey.put(keyBase64, user);

        sendWelcomeMessage(user.getConnection(), keyBase64, user);
        return user;
    }

    public void removeUser(ClientUser clientUser) {
        /*
        Set<Channel> channels = clientUser.getChannels();
        for(Channel channel : channels) {
            channel.getUsers().remove(clientUser);
        }
        */

        clientUser.getKey().ifPresent(k -> clientUsersByPublicKey.remove(k.getBase64SigningPublicKey()));
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
        Optional<ClientUser> target = resolveTargetClientUser(message.getTarget(), message.getSender());

        if(target.isPresent()) {
            //previousNick.ifPresent(n -> message.getSender().getConnection().sendMessage(format(":%s NICK %s", n, target.get().getNick().get())));
            String targetNick = target.get().getNick().get();

            if(!targetNick.equals(message.getTargetText())) {
                sendNickChange(message.getSender(), message.getTargetText(), targetNick);
            }

            Optional<String> previousNick = target.get().addOrUpdateContact(message.getSender());
            previousNick.ifPresent(previous -> sendNickChange(target.get(), previous, message.getSender().getNick().get()));

            target.get().getConnection().sendMessage(message.getSender().getFullyQualifiedName(), MessageType.PRIVMSG.getIrcCommand(), format(":%s", message.getText()));
        } else {
            message.getSender().getConnection().sendMessage(serverName, "401", format("%s :No such nick/channel", message.getTargetText()));
        }
    }

    public void processQuit(QuitMessage message) {

    }

    public void processUser(UserMessage message) {

    }

    public void processPing(PingMessage message) {
        message.getSender().getConnection().sendMessage(serverName, MessageType.PONG.getIrcCommand(), message.getText());
    }

    public void processPong(PongMessage message) {

    }


    private void sendNickChange(ClientUser user, String previousNick, String newNick) {
        user.getConnection().sendMessage(format(":%s NICK %s", previousNick, newNick));
    }

    private Optional<ClientUser> resolveTargetClientUser(Target target, ClientUser sender) {
        if(target.getTargetType() != TargetType.USER)   // TODO: error?
            return Optional.empty(); //Pair.of(Optional.empty(), Optional.empty());

        Optional<ClientUser> targetClientUser = target.getPublicKey().flatMap(this::getClientUserByPublicKey);
        System.out.println(format("%s isPresent=%b", target.getPublicKey().orElse("none"), targetClientUser.isPresent()));

        if(!targetClientUser.isPresent() && target.getNick().isPresent()) {
            Optional<Contact> targetContact = sender.getContactByNick(target.getNick().get());
            if(targetContact.isPresent()) {
                targetClientUser = getClientUserByContact(targetContact.get());
            }
        }

        targetClientUser.ifPresent(sender::addOrUpdateContact);
        return targetClientUser;
        //Optional<String> previousNick = targetClientUser.flatMap(c -> sender.addOrUpdateContact(c.getNick(), c.getKey()));
        //return Pair.of(targetClientUser, previousNick);
    }

    private Optional<ClientUser> getClientUserByPublicKey(String publicKey) {
        return Optional.ofNullable(clientUsersByPublicKey.get(publicKey));
    }

    private Optional<ClientUser> getClientUserByPublicKey(Key key) {
        return getClientUserByPublicKey(key.getBase64SigningPublicKey());
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
