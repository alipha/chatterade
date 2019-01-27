package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class PingMessage extends Message {

    public PingMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public String getText() {
        return getTokenizedMessage().getArgumentText();
    }

    @Override
    public MessageType getType() {
        return MessageType.PING;
    }
}
