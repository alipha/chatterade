package com.liph.chatterade.messaging.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.models.Target;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class JoinMessage extends Message {

    private final Map<Target, Optional<String>> channelKeyMap;


    public JoinMessage(TokenizedMessage tokenizedMessage) {
        super(tokenizedMessage);


        channelKeyMap = new HashMap<>();

        List<String> args = getTokenizedMessage().getArguments();
        int argIndex = 0;

        for(Target channel : getTokenizedMessage().getTargets()) {
            if(argIndex < args.size())
                channelKeyMap.put(channel, Optional.of(args.get(argIndex)));
            else
                channelKeyMap.put(channel, Optional.empty());

            argIndex++;
        }
    }


    public Map<Target, Optional<String>> getChannelKeyMap() {
        return channelKeyMap;
    }


    @Override
    public MessageType getType() {
        return MessageType.JOIN;
    }
}
