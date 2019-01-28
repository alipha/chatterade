package com.liph.chatterade.chat.models;


import com.liph.chatterade.chat.enums.ChatEntityType;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Channel extends ChatEntity {

    private Optional<String> key = Optional.empty();
    private final Set<User> users;


    public Channel(String name, Optional<String> key) {
        super(name);
        this.key = key;
        this.users = ConcurrentHashMap.newKeySet();
    }


    public Optional<String> getKey() {
        return key;
    }

    public void setKey(Optional<String> key) {
        this.key = key;
    }


    public Set<User> getUsers() {
        return users;
    }

    @Override
    public ChatEntityType getType() {
        return ChatEntityType.CHANNEL;
    }
}
