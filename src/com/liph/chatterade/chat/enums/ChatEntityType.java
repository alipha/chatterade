package com.liph.chatterade.chat.enums;


import com.liph.chatterade.chat.models.Channel;
import com.liph.chatterade.chat.models.Server;
import com.liph.chatterade.chat.models.User;

public enum ChatEntityType {
    CHANNEL(Channel.class),
    USER(User.class),
    SERVER(Server.class);

    private Class type;

    ChatEntityType(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
