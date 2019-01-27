package com.liph.chatterade.chat.models;


import com.liph.chatterade.chat.enums.ChatEntityType;

import java.util.Optional;

public abstract class ChatEntity {

    private Optional<String> name = Optional.empty();


    protected ChatEntity() {

    }

    protected ChatEntity(String name) {
        this.name = Optional.of(name);
    }


    public abstract ChatEntityType getType();


    public Optional<String> getName() {
        return name;
    }

    public void setName(Optional<String> name) {
        this.name = name;
    }
}
