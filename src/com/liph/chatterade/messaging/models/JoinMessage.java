package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class JoinMessage extends Message {

    private final Map<String, Optional<String>> channelKeyMap;


    public JoinMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);


        channelKeyMap = new HashMap<>();

        List<String> args = getTokenizedMessage().getArguments();
        int argIndex = 0;

        for(String channel : getTokenizedMessage().getTargetNames()) {
            if(argIndex < args.size())
                channelKeyMap.put(channel, Optional.of(args.get(argIndex)));
            else
                channelKeyMap.put(channel, Optional.empty());

            argIndex++;
        }
    }


    public Map<String, Optional<String>> getChannelKeyMap() {
        return channelKeyMap;
    }


    @Override
    public MessageType getType() {
        return MessageType.JOIN;
    }
}
