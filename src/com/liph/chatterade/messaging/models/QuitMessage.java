package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.Optional;


public class QuitMessage extends Message {

    public QuitMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);
    }

    public Optional<String> getText() {
        return getTokenizedMessage().getArguments().stream().findFirst();
    }

    @Override
    public MessageType getType() {
        return MessageType.QUIT;
    }
}
