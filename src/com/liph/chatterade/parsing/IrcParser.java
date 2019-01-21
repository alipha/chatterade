package com.liph.chatterade.parsing;

import static java.util.stream.Collectors.toList;

import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class IrcParser {

    private final IrcMessageValidator validator;


    public IrcParser() {
        this.validator = new IrcMessageValidator();
    }


    public Message parse(String text, boolean ignoreSender) {
        TokenizedMessage tokenizedMessage = tokenizeMessage(text, ignoreSender);
        validator.validate(tokenizedMessage);

        MessageType messageType = tokenizedMessage.getMessageType().orElseThrow(() -> new IllegalStateException("Unknown message type"));
        return messageType.getConstructor().apply(tokenizedMessage);
    }


    public TokenizedMessage tokenizeMessage(String text, boolean ignoreSender) {
        Optional<String> sender = Optional.empty();
        Optional<MessageType> messageType = Optional.empty();
        Optional<IrcMessageValidationMap> validationType = Optional.empty();
        String messageTypeText = "";
        Optional<String> target = Optional.empty();

        if(text == null) {
            text = "";
        }

        String originalText = text;
        text = text.trim();


        // find last argument and remove it from text
        int trailingArgIndex = text.indexOf(" :", 1);
        Optional<String> trailingArg = Optional.empty();

        if(trailingArgIndex >= 0) {
            trailingArg = Optional.of(text.substring(trailingArgIndex + 2));
            text = text.substring(0, trailingArgIndex);
        }


        // tokenize everything by spaces except the last argument
        List<String> tokens = Arrays.stream(text.split(" +")).collect(toList());


        // find sender (if any) and remove it from token list
        if(!tokens.isEmpty() && tokens.get(0).startsWith(":")) {
            sender = Optional.of(tokens.get(0));
            tokens.remove(0);
        }

        // find the messageType (if any, though having none is a malformed irc message) and remove it from the token list
        if(!tokens.isEmpty()) {
            messageTypeText = tokens.get(0);
            messageType = MessageType.fromIrcCommand(messageTypeText);
            validationType = IrcMessageValidationMap.fromName(messageType.map(Enum::name).orElse(null));
            tokens.remove(0);
        }


        // find the target (if exists and applicable) and remove it from the token list
        if(!tokens.isEmpty() && validationType.map(IrcMessageValidationMap::hasTarget).orElse(false)) {
            target = Optional.of(tokens.get(0));
            tokens.remove(0);
        }


        // the remaining tokens are the arguments to the command, so add the last argument (if there is one) to them
        trailingArg.ifPresent(tokens::add);


        return new TokenizedMessage(
            ignoreSender ? Optional.empty() : sender,
            messageType,
            messageTypeText,
            determineTargetType(target),
            target,
            tokens,
            trailingArgIndex >= 0,
            originalText
        );
    }


    private TargetType determineTargetType(Optional<String> optTarget) {
        if(!optTarget.isPresent())
            return TargetType.NONE;

        String target = optTarget.get();

        if(target.startsWith("#") || target.startsWith("&")) {
            String[] targets = target.split(",");

            if(targets.length == 1)
                return TargetType.CHANNEL;

            if(Stream.of(targets).allMatch(t -> t.startsWith("#") || t.startsWith("&")))
                return TargetType.MULTIPLE_CHANNELS;
            else
                return TargetType.INVALID;   // invalid TargetType, but we don't want to deal with error handling here
        } else {
            return TargetType.USER;
        }
    }
}
