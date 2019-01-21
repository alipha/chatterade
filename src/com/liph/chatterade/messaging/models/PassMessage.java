package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class PassMessage extends Message {

    public PassMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public String getPassword() {
        return getTokenizedMessage().getArguments().get(0);
    }

    @Override
    public MessageType getType() {
        return MessageType.PASS;
    }
}
