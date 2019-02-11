package com.liph.chatterade.parsing.models;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import java.util.List;
import java.util.Optional;


public class TokenizedMessage {

    private final Optional<String> senderName;
    private final Optional<MessageType> messageType;
    private final String messageTypeText;
    private final TargetType targetType;
    private final Optional<String> targetText;
    private final List<Target> targets;
    private final String argumentText;
    private final List<String> arguments;
    private final boolean hasTrailingArgument;
    private final String rawMessage;


    public TokenizedMessage(Optional<String> senderName, Optional<MessageType> messageType, String messageTypeText,
                            TargetType targetType, Optional<String> targetText, List<Target> targets,
                            String argumentText, List<String> arguments, boolean hasTrailingArgument, String rawMessage) {
        this.senderName = senderName;
        this.messageType = messageType;
        this.messageTypeText = messageTypeText;
        this.targetType = targetType;
        this.targetText = targetText;
        this.targets = targets;
        this.argumentText = argumentText;
        this.arguments = arguments;
        this.hasTrailingArgument = hasTrailingArgument;
        this.rawMessage = rawMessage;
    }


    public Optional<String> getSenderName() {
        return senderName;
    }

    public Optional<MessageType> getMessageType() {
        return messageType;
    }

    public String getMessageTypeText() {
        return messageTypeText;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public Optional<String> getTargetText() {
        return targetText;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public String getArgumentText() {
        return argumentText;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean hasTrailingArgument() {
        return hasTrailingArgument;
    }

    public String getRawMessage() {
        return rawMessage;
    }
}
