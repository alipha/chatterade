package com.liph.chatterade.messaging.enums;

import static java.lang.String.format;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.common.QuadConsumer;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.messaging.ClientMessageProcessor;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.messaging.ServerMessageProcessor;
import com.liph.chatterade.messaging.models.Message;
import java.util.Optional;


public enum MessageActionMap {
    JOIN   (wrapClient(ClientMessageProcessor::processJoin),           wrapServer(ServerMessageProcessor::processJoin)),
    NICK   (wrapClient(ClientMessageProcessor::processNick),           wrapServer(ServerMessageProcessor::processNick)),
    NOTICE (wrapClient(ClientMessageProcessor::processNotice),         wrapServer(ServerMessageProcessor::processNotice)),
    PART   (wrapClient(ClientMessageProcessor::processPart),           wrapServer(ServerMessageProcessor::processPart)),
    PASS   (wrapClient(ClientMessageProcessor::processPass),           wrapServer(ServerMessageProcessor::processPass)),
    PRIVMSG(wrapClient(ClientMessageProcessor::processPrivateMessage), wrapServer(ServerMessageProcessor::processPrivateMessage)),
    QUIT   (wrapClient(ClientMessageProcessor::processQuit),           wrapServer(ServerMessageProcessor::processQuit)),
    USER   (wrapClient(ClientMessageProcessor::processUser),           wrapServer(ServerMessageProcessor::processUser)),
    PING   (wrapClient(ClientMessageProcessor::processPing),           wrapServer(ServerMessageProcessor::processPing)),
    PONG   (wrapClient(ClientMessageProcessor::processPong),           wrapServer(ServerMessageProcessor::processPong)),
    CONNECT(wrapClient(ClientMessageProcessor::processConnect),        wrapServer(ServerMessageProcessor::processConnect));
    
    
    private QuadConsumer<ClientMessageProcessor, Message, ClientUser, ClientConnection> clientMessageConsumer;
    private QuadConsumer<ServerMessageProcessor, Message, Contact, ClientUser> serverMessageConsumer;
    
    
    MessageActionMap(QuadConsumer<ClientMessageProcessor, Message, ClientUser, ClientConnection> clientMessageConsumer, QuadConsumer<ServerMessageProcessor, Message, Contact, ClientUser> serverMessageConsumer) {
        this.clientMessageConsumer = clientMessageConsumer;
        this.serverMessageConsumer = serverMessageConsumer;
    }
    
    
    public static void process(ClientMessageProcessor processor, Message message, ClientUser sender, ClientConnection connection) {
        MessageActionMap messageAction = fromName(message.getType().name())
            .orElseThrow(() -> new IllegalStateException(format("%s is missing in MessageActionMap.", message.getType().name())));

        messageAction.clientMessageConsumer.accept(processor, message, sender, connection);
    }

    public static void process(ServerMessageProcessor processor, Message message, Contact sender, ClientUser recipient) {
        MessageActionMap messageAction = fromName(message.getType().name())
            .orElseThrow(() -> new IllegalStateException(format("%s is missing in MessageActionMap.", message.getType().name())));

        messageAction.serverMessageConsumer.accept(processor, message, sender, recipient);
    }

    public static Optional<MessageActionMap> fromName(String name) {
        return EnumHelper.fromName(values(), name);
    }


    private static <T extends Message> QuadConsumer<ClientMessageProcessor, Message, ClientUser, ClientConnection> wrapClient(QuadConsumer<ClientMessageProcessor, T, ClientUser, ClientConnection> consumer) {
        return (a, m, s, c) -> consumer.accept(a, (T)m, s, c);
    }

    private static <T extends Message> QuadConsumer<ServerMessageProcessor, Message, Contact, ClientUser> wrapServer(QuadConsumer<ServerMessageProcessor, T, Contact, ClientUser> consumer) {
        return (a, m, s, r) -> consumer.accept(a, (T)m, s, r);
    }
}
