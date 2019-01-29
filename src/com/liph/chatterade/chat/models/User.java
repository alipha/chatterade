package com.liph.chatterade.chat.models;


import static java.lang.String.format;

import com.liph.chatterade.chat.enums.ChatEntityType;
import com.liph.chatterade.encryption.models.Key;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class User extends ChatEntity {

    private Optional<String> username = Optional.empty();
    private Optional<String> realName = Optional.empty();
    private Optional<Key> key = Optional.empty();


    public User() {}

    public User(String nick) {
        super(nick);
    }

    public User(String nick, String username, String realName) {
        super(nick);
        this.username = Optional.of(username);
        this.realName = Optional.of(realName);
    }

    public User(Optional<String> nick, Optional<String> username, Optional<Key> key) {
        setNick(nick);
        this.username = username;
        this.key = key;
    }


    public Optional<String> getNick() {
        return getName();
    }

    public void setNick(Optional<String> nick) {
        setName(nick);
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

    public Optional<Key> getKey() {
        return key;
    }

    public void setKey(Optional<Key> key) {
        this.key = key;
    }


    public String getFullyQualifiedName() {
        String publicKey = getKey().map(Key::getBase64SigningPublicKey).orElse("unknown");
        return format("%s!%s@%s", getNick().orElse("unknown"), getUsername().orElse("unknown"), publicKey);
    }


    @Override
    public ChatEntityType getType() {
        return ChatEntityType.USER;
    }
}
