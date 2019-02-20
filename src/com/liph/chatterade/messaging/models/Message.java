package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public abstract class Message {

    private final TokenizedMessage tokenizedMessage;


    protected Message(TokenizedMessage tokenizedMessage) {
        this.tokenizedMessage = tokenizedMessage;
    }


    public abstract MessageType getType();


    public TokenizedMessage getTokenizedMessage() {
        return tokenizedMessage;
    }
}
