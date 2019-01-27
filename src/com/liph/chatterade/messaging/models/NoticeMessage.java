package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.Target;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class NoticeMessage extends Message {

    public NoticeMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public Target getTarget() {
        return getTokenizedMessage().getTargets().get(0);
    }

    public String getText() {
        return getTokenizedMessage().getArguments().get(0);
    }

    @Override
    public MessageType getType() {
        return MessageType.NOTICE;
    }
}
