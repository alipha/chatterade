package com.liph.chatterade.chat.models;


import com.liph.chatterade.encryption.models.PublicKey;
import java.util.Optional;


public class Contact extends User {

    public Contact(Optional<String> nick, Optional<PublicKey> publicKey) {
        setNick(nick);
        setPublicKey(publicKey);
    }
}
