package com.liph.chatterade.chat.models;


import com.liph.chatterade.encryption.models.PublicKey;
import java.util.Optional;


public class Contact {

    private Optional<String> nick;
    private Optional<String> username = Optional.empty();
    private PublicKey publicKey;


    public Contact(Optional<String> nick, PublicKey publicKey) {
        this.nick = nick;
        this.publicKey = publicKey;
    }

    public Contact(Optional<String> nick, Optional<String> username, PublicKey publicKey) {
        this.nick = nick;
        this.username = username;
        this.publicKey = publicKey;
    }


    public Optional<String> getNick() {
        return nick;
    }

    public void setNick(Optional<String> nick) {
        this.nick = nick;
    }


    public Optional<String> getUsername() {
        return username;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
