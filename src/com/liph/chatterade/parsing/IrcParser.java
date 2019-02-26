package com.liph.chatterade.parsing;

import static java.util.stream.Collectors.toList;

import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.models.PublicKey;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import com.liph.chatterade.parsing.models.Target;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        Optional<String> targetText = Optional.empty();
        List<Target> targets = Collections.emptyList();

        if(text == null) {
            text = "";
        }

        text = text.replaceAll("[\\r\\n\0]", "");
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
            sender = Optional.of(tokens.get(0).substring(1));
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
            targetText = Optional.of(tokens.get(0));
            String[] targetTexts = tokens.get(0).split(",");
            tokens.remove(0);

            targets = Stream.of(targetTexts).map(this::parseTarget).collect(toList());
        }

        String argumentText = String.join(" ", tokens) + trailingArg.map(a -> " :" + a).orElse("");

        // the remaining tokens are the arguments to the command, so add the last argument (if there is one) to them
        trailingArg.ifPresent(tokens::add);


        return new TokenizedMessage(
            ignoreSender ? Optional.empty() : sender,
            messageType,
            messageTypeText,
            determineTargetType(targets),
            targetText,
            targets,
            argumentText.trim(),
            tokens,
            trailingArgIndex >= 0,
            originalText
        );
    }


    private TargetType determineTargetType(List<Target> targets) {
        if(targets.isEmpty())
            return TargetType.NONE;

        if(targets.size() > 1) {
            if(targets.stream().allMatch(t -> t.getTargetType() == TargetType.CHANNEL))
                return TargetType.MULTIPLE_CHANNELS;
            else
                return TargetType.INVALID;   // invalid TargetType, but we don't want to deal with error handling here
        }

        return targets.get(0).getTargetType();
    }


    private Target parseTarget(String targetText) {
        TargetType targetType = TargetType.INVALID;
        Optional<String> nick = Optional.empty();
        Optional<String> channel = Optional.empty();
        Optional<String> publicKey = Optional.empty();

        String[] targetParts = targetText.split("\\^");

        if(targetParts.length == 2) {
            publicKey = Optional.of(targetParts[1]);
        }

        if(targetParts.length <= 2) {
            if(targetParts[0].startsWith("#") || targetParts[0].startsWith("&")) {
                targetType = TargetType.CHANNEL;

                if(targetParts[0].length() == 53) {     // TODO: better public key identification
                    publicKey = Optional.of(targetParts[0].substring(1));
                } else {
                    channel = Optional.of(targetParts[0]);
                }
            } else {
                targetType = TargetType.USER;

                if(targetParts[0].length() == 52) {     // TODO: better public key identification
                    publicKey = Optional.of(targetParts[0]);
                } else {
                    nick = Optional.of(targetParts[0]);
                }
            }
        }

        return new Target(targetType, nick, channel, publicKey);
    }


    public Optional<Contact> parseSender(Optional<String> sender, ByteArray publicKey) {
        Optional<String> nick = Optional.empty();
        Optional<String> username = Optional.empty();

        if(!sender.isPresent() || sender.get().isEmpty())
            return Optional.empty();

        String senderText = sender.get();

        int at = senderText.lastIndexOf('@');

        if(at > -1) {
            senderText = senderText.substring(0, at);
        }

        int bang = senderText.lastIndexOf('!');

        if(bang > -1) {
            username = Optional.of(senderText.substring(bang + 1));
            senderText = senderText.substring(0, bang);
        }

        if(!senderText.isEmpty())
            nick = Optional.of(senderText);

        return Optional.of(new Contact(nick, username, new PublicKey(publicKey)));
    }
}
