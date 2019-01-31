package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.Optional;


public class ConnectMessage extends Message {

    public ConnectMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public String getServer() {
        return getTokenizedMessage().getArguments().get(0);
    }

    public Optional<Integer> getPort() {
        if(getTokenizedMessage().getArguments().size() > 1)
            return Optional.of(Integer.parseInt(getTokenizedMessage().getArguments().get(1)));
        else
            return Optional.empty();
    }

    @Override
    public MessageType getType() {
        return MessageType.CONNECT;
    }
}
