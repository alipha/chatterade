package com.liph.chatterade.chat.models;

import com.liph.chatterade.connection.ClientConnection;


public class ClientUser extends User {

    private final ClientConnection connection;


    public ClientUser(String nick, String username, String realName, ClientConnection connection) {
        super(nick, username, realName);
        this.connection = connection;
    }


    public ClientConnection getConnection() {
        return connection;
    }
}
