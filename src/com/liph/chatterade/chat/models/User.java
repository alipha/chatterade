package com.liph.chatterade.chat.models;


import static java.lang.String.format;

import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.encryption.models.PublicKey;
import java.util.Optional;


public class User {

    private Optional<String> nick = Optional.empty();
    private Optional<String> username = Optional.empty();
    private Optional<String> realName = Optional.empty();
    private Optional<PublicKey> publicKey = Optional.empty();


    public User() {}

    public User(String nick) {
        this.nick = Optional.of(nick);
    }

    public User(String nick, PublicKey publicKey) {
        this.nick = Optional.of(nick);
        this.publicKey = Optional.of(publicKey);
    }

    public User(String nick, String username, String realName) {
        this.nick = Optional.of(nick);
        this.username = Optional.of(username);
        this.realName = Optional.of(realName);
    }

    public User(Optional<String> nick, Optional<String> username, Optional<PublicKey> publicKey) {
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

    public void setUsername(Optional<String> username) {
        this.username = username;
    }

    public Optional<String> getRealName() {
        return realName;
    }

    public void setRealName(Optional<String> realName) {
        this.realName = realName;
    }

    public Optional<PublicKey> getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Optional<PublicKey> publicKey) {
        this.publicKey = publicKey;
    }


    public String getFullyQualifiedName() {
        String publicKey = getPublicKey().map(Key::getBase32SigningKey).orElse("unknown");
        return format("%s!%s@%s", getNick().orElse("unknown"), getUsername().orElse("unknown"), publicKey);
    }
}
