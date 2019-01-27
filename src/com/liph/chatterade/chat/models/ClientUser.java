package com.liph.chatterade.chat.models;

import static java.lang.String.format;

import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.encryption.models.Key;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class ClientUser extends User {

    private final ClientConnection connection;
    private final Map<String, Contact> contactsByNick;


    public ClientUser(String nick, String username, String realName, ClientConnection connection) {
        super(nick, username, realName);
        this.connection = connection;
        this.contactsByNick = new ConcurrentHashMap<>();
    }


    public String getFullyQualifiedName() {
        String publicKey = getKey().map(Key::getBase64SigningPublicKey).orElse("unknown");
        return format("%s!%s@%s", getNick(), getUsername().orElse(getNick()), publicKey);
    }


    public void addContact(Contact user) {
        this.contactsByNick.put(user.getNick().toLowerCase(), user);
    }


    public Optional<Contact> getContactByNick(String nick) {
        return Optional.ofNullable(this.contactsByNick.get(nick.toLowerCase()));
    }


    public ClientConnection getConnection() {
        return connection;
    }
}
