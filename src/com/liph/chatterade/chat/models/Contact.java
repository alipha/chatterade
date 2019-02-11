package com.liph.chatterade.chat.models;


import com.liph.chatterade.encryption.models.PublicKey;
import java.util.Optional;


public class Contact {

    private Optional<String> nick = Optional.empty();
    private PublicKey publicKey;


    public Contact(Optional<String> nick, PublicKey publicKey) {
        this.nick = nick;
        this.publicKey = publicKey;
    }


    public Optional<String> getNick() {
        return nick;
    }

    public void setNick(Optional<String> nick) {
        this.nick = nick;
    }


    public PublicKey getPublicKey() {
        return publicKey;
    }
}
