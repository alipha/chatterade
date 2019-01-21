package com.liph.chatterade.chat.models;


import com.liph.chatterade.chat.enums.ChatEntityType;

public abstract class ChatEntity {

    private String name;


    protected ChatEntity(String name) {
        this.name = name;
    }


    public abstract ChatEntityType getType();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
