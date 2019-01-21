package com.liph.chatterade.parsing;

import static java.lang.String.format;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import com.liph.chatterade.parsing.exceptions.MalformedIrcMessageException;
import com.liph.chatterade.parsing.models.TokenizedMessage;


public class IrcMessageValidator {

    public void validate(TokenizedMessage message) {
        doGenericValidation(message);
    }


    private void doGenericValidation(TokenizedMessage message) {
        MessageType messageType = message.getMessageType()
            .orElseThrow(() -> new MalformedIrcMessageException(message.getMessageTypeText(), "421", format("%s :Unknown command", message.getMessageTypeText())));

        IrcMessageValidationMap validationType = IrcMessageValidationMap.fromName(messageType.name())
            .orElseThrow(() -> new IllegalStateException(format("%s is missing an IrcMessageValidationMap.", messageType.name())));

        if(validationType.isTargetTypeAllowed(message.getTargetType())) {
            if(message.getTargetType() == TargetType.NONE)
                throw new MalformedIrcMessageException(messageType.getIrcCommand(), "411", format(":No recipient given (%s)", messageType.getIrcCommand()));

            throw new IllegalArgumentException(format("%s is an invalid target for command %s.", message.getTargetName().orElse("(none)"), messageType.getIrcCommand()));
        }

        if(message.getArguments().size() < validationType.getMinimumArgumentCount())
            throw new MalformedIrcMessageException(messageType.getIrcCommand(), "461", format("%s :Not enough parameters", messageType.getIrcCommand()));

        /*
        if(validationType.getMaximumArgumentCount().isPresent() && message.getArguments().size() > validationType.getMaximumArgumentCount().get())
            throw new IllegalArgumentException(format("Too many arguments to %s. No more than %d arguments.", messageType.getIrcCommand(), validationType.getMaximumArgumentCount().get()));

        if(message.hasTrailingArgument() && validationType.getTrailingArgumentRequirement() == RequirementType.DISALLOWED)
            throw new IllegalArgumentException(format("%s is not allowed to have an argument beginning with :", messageType.getIrcCommand()));

        if(!message.hasTrailingArgument() && validationType.getTrailingArgumentRequirement() == RequirementType.REQUIRED)
            throw new IllegalArgumentException(format("%s must have an argument beginning with :", messageType.getIrcCommand()));
        */
    }
}
