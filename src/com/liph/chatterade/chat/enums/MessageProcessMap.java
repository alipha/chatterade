package com.liph.chatterade.chat.enums;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.messaging.models.Message;
import java.util.Optional;
import java.util.function.BiConsumer;


public enum MessageProcessMap {
    JOIN   (wrap(Application::processJoin)),
    NICK   (wrap(Application::processNick)),
    NOTICE (wrap(Application::processNotice)),
    PART   (wrap(Application::processPart)),
    PASS   (wrap(Application::processPass)),
    PRIVMSG(wrap(Application::processPrivateMessage)),
    QUIT   (wrap(Application::processQuit)),
    USER   (wrap(Application::processUser)),
    PING   (wrap(Application::processPing)),
    PONG   (wrap(Application::processPong)),
    CONNECT(wrap(Application::processConnect));
    
    
    private BiConsumer<Application, Message> consumer;
    
    
    MessageProcessMap(BiConsumer<Application, Message> consumer) {
        this.consumer = consumer;
    }
    
    
    public static void process(Application application, Message message) {
        MessageProcessMap processor = fromName(message.getType().name())
            .orElseThrow(() -> new IllegalStateException(format("%s is missing in MessageProcessMap.", message.getType().name())));
        
        processor.consumer.accept(application, message);
    }

    public static Optional<MessageProcessMap> fromName(String name) {
        return EnumHelper.fromName(values(), name);
    }


    private static <T extends Message> BiConsumer<Application, Message> wrap(BiConsumer<Application, T> consumer) {
        return (a, m) -> consumer.accept(a, (T)m);
    }
}
