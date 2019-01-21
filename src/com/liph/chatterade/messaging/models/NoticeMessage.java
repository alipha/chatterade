package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class NoticeMessage extends Message {

    public NoticeMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public String getTarget() {
        return getTokenizedMessage().getTargetName().get();
    }

    public String getText() {
        return getTokenizedMessage().getArguments().get(0);
    }

    @Override
    public MessageType getType() {
        return MessageType.NOTICE;
    }
}
