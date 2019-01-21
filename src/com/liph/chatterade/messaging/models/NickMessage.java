package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class NickMessage extends Message {

    public NickMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public String getNewNick() {
        return getTokenizedMessage().getArguments().get(0);
    }

    @Override
    public MessageType getType() {
        return MessageType.NICK;
    }
}
