package com.liph.chatterade.chat.models;

import static java.lang.String.format;

import com.liph.chatterade.connection.ClientConnection;


public class ClientUser extends User {

    private final ClientConnection connection;


    public ClientUser(String nick, String username, String realName, ClientConnection connection) {
        super(nick, username, realName);
        this.connection = connection;
    }


    public String getFullyQualifiedName() {
        return format("%s!%s@localhost", getNick(), getUsername());
    }

    public ClientConnection getConnection() {
        return connection;
    }
}
