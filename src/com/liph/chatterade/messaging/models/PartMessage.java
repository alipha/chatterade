package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.Target;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.List;


public class PartMessage extends Message {

    public PartMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public List<Target> getChannels() {
        return getTokenizedMessage().getTargets();
    }

    @Override
    public MessageType getType() {
        return MessageType.PART;
    }
}
