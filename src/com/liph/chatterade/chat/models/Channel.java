package com.liph.chatterade.chat.models;


import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Channel {

    private final String name;
    private final Set<Contact> users;
    private Optional<String> key = Optional.empty();


    public Channel(String name, Optional<String> key) {
        this.name = name;
        this.key = key;
        this.users = ConcurrentHashMap.newKeySet();
    }


    public String getName() {
        return name;
    }


    public Set<Contact> getUsers() {
        return users;
    }


    public Optional<String> getKey() {
        return key;
    }

    public void setKey(Optional<String> key) {
        this.key = key;
    }
}
