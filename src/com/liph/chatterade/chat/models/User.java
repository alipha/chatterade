package com.liph.chatterade.chat.models;


import com.liph.chatterade.chat.enums.ChatEntityType;
import com.liph.chatterade.encryption.models.Key;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class User extends ChatEntity {

    private Optional<String> username;
    private Optional<String> realName;
    private final Set<Channel> channels;
    private final Set<Key> keys;


    public User(String nick) {
        super(nick);
        username = Optional.empty();
        realName = Optional.empty();
        channels = ConcurrentHashMap.newKeySet();
        keys = ConcurrentHashMap.newKeySet();
    }

    public User(String nick, String username, String realName) {
        super(nick);
        this.username = Optional.of(username);
        this.realName = Optional.of(realName);
        this.channels = ConcurrentHashMap.newKeySet();
        keys = ConcurrentHashMap.newKeySet();
    }


    public String getNick() {
        return getName();
    }

    public void setNick(String nick) {
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

    public Set<Channel> getChannels() {
        return channels;
    }

    public Set<Key> getKeys() {
        return keys;
    }

    @Override
    public ChatEntityType getType() {
        return ChatEntityType.USER;
    }
}
