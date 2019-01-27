package com.liph.chatterade.chat.models;


import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.parsing.models.Target;

import java.util.Optional;

public class Contact extends User {

    private boolean sentNickMessage;


    public Contact(String nick) {
        super(nick);
    }

    public Contact(Target target) {
        setNick(target.getNick());
        target.getPublicKey().ifPresent(k -> setKey(Optional.of(new Key(k))));
    }

    public Contact(Optional<String> nick, Optional<Key> key) {
        setNick(nick);
        setKey(key);
    }


    public boolean hasSentNickMessage() {
        return sentNickMessage;
    }

    public void setSentNickMessage(boolean sentNickMessage) {
        this.sentNickMessage = sentNickMessage;
    }
}
