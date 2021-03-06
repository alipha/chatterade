package com.liph.chatterade.chat;


import static java.lang.String.format;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.ResolveTargetResult;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.common.LockManager;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.PublicKey;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.parsing.IrcFormatter;
import com.liph.chatterade.parsing.models.Target;
import com.liph.chatterade.serialization.Serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class ClientUserManager {

    private final Application application;
    private final IrcFormatter ircFormatter;
    private final Serializer serializer;
    private final LockManager lockManager;
    private final Map<ByteArray, ClientUser> clientUsersByPublicKey;
    private final Map<ByteArray, ClientUser> clientUsersByPasswordKey;


    public ClientUserManager(Application application) {
        this.application = application;
        this.ircFormatter = application.getIrcFormatter();
        this.serializer = application.getSerializer();
        this.lockManager = application.getLockManager();
        this.clientUsersByPublicKey = new ConcurrentHashMap<>();
        this.clientUsersByPasswordKey = new ConcurrentHashMap<>();
    }


    public Collection<ClientUser> getUsers() {
        return clientUsersByPublicKey.values();
    }


    public ClientUser addUser(String nick, Optional<ByteArray> passwordKey, ClientConnection connection) {
        if(!passwordKey.isPresent())
            return doAddUser(nick, passwordKey, connection);

        return lockManager.with(passwordKey.get(), () -> doAddUser(nick, passwordKey, connection));
    }

    private ClientUser doAddUser(String nick, Optional<ByteArray> passwordKey, ClientConnection connection) {
        Optional<ClientUser> existingUser = passwordKey.flatMap(k -> Optional.ofNullable(clientUsersByPasswordKey.get(k)));
        ClientUser user;

        if(existingUser.isPresent()) {
            user = existingUser.get();
            user.addConnection(connection);
        } else {
            user = serializer.load(passwordKey, keyPair -> new ClientUser(nick, passwordKey, keyPair, connection));

            clientUsersByPublicKey.put(user.getPublicKey().getSigningKey(), user);
            passwordKey.ifPresent(k -> clientUsersByPasswordKey.put(k, user));
        }

        return user;
    }


    public Optional<String> addOrUpdateContact(ClientUser user, Contact contact) {
        Optional<String> previousNick = user.addOrUpdateContact(contact);
        serializer.delayedSave(user);
        return previousNick;
    }


    public void removeUser(ClientUser clientUser) {
        /*
        Set<Channel> channels = clientUser.getChannels();
        for(Channel channel : channels) {
            channel.getUsers().remove(clientUser);
        }
        */
        //clientUsersByPublicKey.remove(clientUser.getPublicKey().getSigningKey());
    }


    public ResolveTargetResult resolveTargetUser(Target target, Optional<ClientUser> sender) {
        if(target.getTargetType() != TargetType.USER)   // TODO: error?
            return new ResolveTargetResult();

        Optional<Contact> targetRemoteUser = Optional.empty();
        Optional<Contact> targetContact = Optional.empty();

        if(target.getPublicKey().isPresent())
            targetRemoteUser = Optional.of(new Contact(target.getNick(), target.getPublicKey().map(PublicKey::new).get()));

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
                addOrUpdateContact(sender.get(), targetClientUser.get().asContact());
            else if(targetRemoteUser.isPresent() && targetRemoteUser.get().getNick().isPresent())
                addOrUpdateContact(sender.get(), targetRemoteUser.get());
        }

        if(targetClientUser.isPresent())
            return new ResolveTargetResult(Optional.empty(), targetClientUser);
        else if(targetContact.isPresent())
            return new ResolveTargetResult(targetContact, Optional.empty());
        else
            return new ResolveTargetResult(targetRemoteUser, Optional.empty());
        //Optional<String> previousNick = targetClientUser.flatMap(c -> sender.addOrUpdateContact(c.getNick(), c.getKey()));
        //return Pair.of(targetClientUser, previousNick);
    }


    public void sendNetworkMessage(ClientUser sender, MessageType messageType, Contact target, String arguments) {
        String targetPublicKey = target.getPublicKey().getBase32SigningKey();
        String message = ircFormatter.formatMessage(ircFormatter.getFullyQualifiedName(sender), messageType.getIrcCommand(), targetPublicKey, arguments);

        byte[] encryptedMessage = EncryptionService.getInstance().encryptMessage(sender.getKeyPair(), target.getPublicKey(), application.getRecentMessageManager().getMostRecentMessage(), message);
        application.relayMessage(encryptedMessage, Optional.empty());
    }


    public void sendWelcomeMessage(ClientConnection connection, ClientUser user) {
        String keyBase32 = user.getPublicKey().getBase32SigningKey();
        String serverName = application.getServerName();
        connection.sendMessage(serverName, "001", format(":Welcome to the Internet Relay Network %s", ircFormatter.getFullyQualifiedName(user)));
        connection.sendMessage(serverName, "002", format(":Your host is %s, running version %s", serverName, application.getServerVersion()));
        connection.sendMessage(serverName, "003", format(":This server was created %s", application.getStartupTime()));
        connection.sendMessage(serverName, "004", format("%s %s DOQRSZaghilopswz CFILMPQSbcefgijklmnopqrstvz bkloveqjfI", serverName, application.getServerVersion()));
        connection.sendMessage(serverName, "375", format(":- %s Message of the Day -", serverName));
        connection.sendMessage(serverName, "372", "Welcome to my test chatterade server!");
        connection.sendMessage(serverName, "376", ":End of /MOTD command.");
        connection.sendMessage(serverName, "NOTICE", format(":Your public key is %s. Users need to /msg %s^%s to message you.", keyBase32, user.getNick(), keyBase32));
    }


    private Optional<ClientUser> getUserByPublicKey(ByteArray publicKey) {
        return Optional.ofNullable(clientUsersByPublicKey.get(publicKey));
    }

    private Optional<ClientUser> getUserByContact(Contact contact) {
        return getUserByPublicKey(contact.getPublicKey().getSigningKey());
    }
}
