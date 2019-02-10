package com.liph.chatterade.chat.enums;

import static java.lang.String.format;

import com.liph.chatterade.chat.MessageProcessor;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.messaging.models.Message;
import java.util.Optional;
import java.util.function.BiConsumer;


public enum MessageProcessMap {
    JOIN   (wrap(MessageProcessor::processJoin)),
    NICK   (wrap(MessageProcessor::processNick)),
    NOTICE (wrap(MessageProcessor::processNotice)),
    PART   (wrap(MessageProcessor::processPart)),
    PASS   (wrap(MessageProcessor::processPass)),
    PRIVMSG(wrap(MessageProcessor::processPrivateMessage)),
    QUIT   (wrap(MessageProcessor::processQuit)),
    USER   (wrap(MessageProcessor::processUser)),
    PING   (wrap(MessageProcessor::processPing)),
    PONG   (wrap(MessageProcessor::processPong)),
    CONNECT(wrap(MessageProcessor::processConnect));
    
    
    private BiConsumer<MessageProcessor, Message> consumer;
    
    
    MessageProcessMap(BiConsumer<MessageProcessor, Message> consumer) {
        this.consumer = consumer;
    }
    
    
    public static void process(MessageProcessor processor, Message message) {
        MessageProcessMap messageAction = fromName(message.getType().name())
            .orElseThrow(() -> new IllegalStateException(format("%s is missing in MessageProcessMap.", message.getType().name())));

        messageAction.consumer.accept(processor, message);
    }

    public static Optional<MessageProcessMap> fromName(String name) {
        return EnumHelper.fromName(values(), name);
    }


    private static <T extends Message> BiConsumer<MessageProcessor, Message> wrap(BiConsumer<MessageProcessor, T> consumer) {
        return (a, m) -> consumer.accept(a, (T)m);
    }
}
