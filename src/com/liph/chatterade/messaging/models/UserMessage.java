package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class UserMessage extends Message {

    public UserMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public String getUsername() {
        return getTokenizedMessage().getArguments().get(0);
    }

    public String getHostname() {
        return getTokenizedMessage().getArguments().get(1);
    }

    public String getServerName() {
        return getTokenizedMessage().getArguments().get(2);
    }

    public String getRealName() {
        return getTokenizedMessage().getArguments().get(3);
    }


    @Override
    public MessageType getType() {
        return MessageType.USER;
    }
}
