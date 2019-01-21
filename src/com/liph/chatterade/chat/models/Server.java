package com.liph.chatterade.chat.models;


import com.liph.chatterade.chat.enums.ChatEntityType;

public class Server extends ChatEntity {

    public Server(String name) {
        super(name);
    }

    @Override
    public ChatEntityType getType() {
        return ChatEntityType.SERVER;
    }
}
