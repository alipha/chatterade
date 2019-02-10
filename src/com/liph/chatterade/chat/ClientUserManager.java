package com.liph.chatterade.chat;


import static java.lang.String.format;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.parsing.models.Target;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class ClientUserManager {

    private final Application application;
    private final Map<ByteArray, ClientUser> clientUsersByPublicKey;


    public ClientUserManager(Application application) {
        this.application = application;
        this.clientUsersByPublicKey = new ConcurrentHashMap<>();
    }


    public Collection<ClientUser> getUsers() {
        return clientUsersByPublicKey.values();
    }


    public ClientUser addUser(ClientUser user) {
        Key key = EncryptionService.getInstance().generateKey();
        user.setKey(Optional.of(key));

        clientUsersByPublicKey.put(key.getSigningPublicKey(), user);

        sendWelcomeMessage(user.getConnection(), key.getBase32SigningPublicKey(), user);
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


    public Optional<User> resolveTargetUser(Target target, Optional<ClientUser> sender) {
        if(target.getTargetType() != TargetType.USER)   // TODO: error?
            return Optional.empty(); //Pair.of(Optional.empty(), Optional.empty());

        Optional<User> targetRemoteUser = Optional.empty();
        Optional<Contact> targetContact = Optional.empty();

        if(target.getPublicKey().isPresent())
            targetRemoteUser = Optional.of(new User(target.getNick(), Optional.empty(), target.getPublicKey().map(Key::new)));

        Optional<ClientUser> targetClientUser = target.getPublicKeyBytes().flatMap(this::getUserByPublicKey);
        System.out.println(format("%s isPresent=%b", target.getPublicKey().orElse("none"), targetClientUser.isPresent()));

        if(sender.isPresent()) {
            if (!targetClientUser.isPresent() && target.getNick().isPresent()) {
                targetContact = sender.get().getContactByNick(target.getNick().get());
                if (targetContact.isPresent()) {
                    targetClientUser = getUserByContact(targetContact.get());
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


    public void sendNetworkMessage(User sender, MessageType messageType, User target, String arguments) {
        String targetPublicKey = target.getKey().get().getBase32SigningPublicKey();
        String message = format(":%s %s %s %s", sender.getFullyQualifiedName(), messageType.getIrcCommand(), targetPublicKey, arguments);

        byte[] encryptedMessage = EncryptionService.getInstance().encryptMessage(sender.getKey().get(), target.getKey().get(), application.getRecentMessageManager().getMostRecentMessage(), message);
        application.relayMessage(encryptedMessage, Optional.empty());
    }


    private void sendWelcomeMessage(ClientConnection connection, String keyBase32, ClientUser user) {
        String serverName = application.getServerName();
        connection.sendMessage(serverName, "001", format(":Welcome to the Internet Relay Network %s", user.getFullyQualifiedName()));
        connection.sendMessage(serverName, "002", format(":Your host is %s, running version %s", serverName, application.getServerVersion()));
        connection.sendMessage(serverName, "003", format(":This server was created %s", application.getStartupTime()));
        connection.sendMessage(serverName, "004", format("%s %s DOQRSZaghilopswz CFILMPQSbcefgijklmnopqrstvz bkloveqjfI", serverName, application.getServerVersion()));
        connection.sendMessage(serverName, "375", format(":- %s Message of the Day -", serverName));
        connection.sendMessage(serverName, "372", "Welcome to my test chatterade server!");
        connection.sendMessage(serverName, "376", ":End of /MOTD command.");
        connection.sendMessage(serverName, "NOTICE", format(":Your public key is %s. Users need to /msg %s^%s to message you.", keyBase32, user.getNick().get(), keyBase32));
    }


    private Optional<ClientUser> getUserByPublicKey(ByteArray publicKey) {
        return Optional.ofNullable(clientUsersByPublicKey.get(publicKey));
    }

    private Optional<ClientUser> getUserByPublicKey(Key key) {
        return getUserByPublicKey(key.getSigningPublicKey());
    }

    private Optional<ClientUser> getUserByContact(User user) {
        return user.getKey().flatMap(this::getUserByPublicKey);
    }
}
