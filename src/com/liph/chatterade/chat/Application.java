package com.liph.chatterade.chat;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.liph.chatterade.chat.enums.MessageProcessMap;
import com.liph.chatterade.chat.models.Channel;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.Server;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.common.Pair;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.connection.ConnectionListener;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.connection.models.RecentMessage;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.messaging.models.*;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import com.liph.chatterade.parsing.models.Target;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class Application {

    private final Instant startupTime;
    private final String serverName;
    private final String serverVersion;

    private final EncryptionService encryptionService;
    private final Set<ServerConnection> serverConnections;

    private RecentMessageManager recentMessageManager;
    private ClientUserManager clientUserManager;
    private ClientMessageProcessor clientMessageProcessor;
    private ServerMessageProcessor serverMessageProcessor;


    public Application(String serverName, String serverVersion, EncryptionService encryptionService) {
        this.startupTime = Instant.now();
        this.serverName = serverName;
        this.serverVersion = serverVersion;

        this.encryptionService = encryptionService;
        this.serverConnections = ConcurrentHashMap.newKeySet();
    }


    public void run(RecentMessageManager recentMessageManager, ClientUserManager clientUserManager,
                    ClientMessageProcessor clientMessageProcessor, ServerMessageProcessor serverMessageProcessor,
                    List<ConnectionListener> connectionListeners) {
        verifyCodeConsistency();

        this.recentMessageManager = recentMessageManager;
        this.clientUserManager = clientUserManager;
        this.clientMessageProcessor = clientMessageProcessor;
        this.serverMessageProcessor = serverMessageProcessor;

        List<Thread> listenerThreads = connectionListeners.stream().map(Thread::new).collect(toList());

        for(Thread thread : listenerThreads)
            thread.start();

        for(Thread thread : listenerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public String getServerName() {
        return serverName;
    }

    public Instant getStartupTime() {
        return startupTime;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public RecentMessageManager getRecentMessageManager() {
        return recentMessageManager;
    }

    public ClientUserManager getClientUserManager() {
        return clientUserManager;
    }

    public ClientMessageProcessor getClientMessageProcessor() {
        return clientMessageProcessor;
    }

    public ServerMessageProcessor getServerMessageProcessor() {
        return serverMessageProcessor;
    }


    public void removeServer(ServerConnection server) {
        serverConnections.remove(server);
    }


    public void addServerConnection(ServerConnection server) {
        serverConnections.add(server);
        System.out.println("Server added.");
    }


    public boolean relayMessage(byte[] message, Optional<ServerConnection> originator) {
        RecentMessage recentMessage = new RecentMessage(encryptionService.getMessageHash(message));

        if(!recentMessageManager.addToRecentMessages(recentMessage, originator))
            return false;

        for(ServerConnection connection : serverConnections) {
            // !originator.isPresent() is simply an optimization
            if(!originator.isPresent() || !recentMessage.getOriginators().contains(connection))
                connection.sendMessage(message);
        }

        return true;
    }


    public Optional<DecryptedMessage> decryptMessage(byte[] encryptedMessage) {

        for(ClientUser user : clientUserManager.getUsers()) {
            Optional<DecryptedMessage> decryptedMessage = encryptionService.decryptMessage(user.getKey().get(), encryptedMessage);
            if(decryptedMessage.isPresent())
                return decryptedMessage;
        }

        return Optional.empty();
    }


    public void sendNickChange(ClientUser user, String previousNick, User contact) {
        String publicKey = contact.getKey().map(Key::getBase32SigningPublicKey).orElse("unknown");
        user.getConnection().sendMessage(format(":%s!%s@%s NICK %s", previousNick, contact.getUsername().orElse("unknown"), publicKey, contact.getNick().get()));
    }


    private void verifyCodeConsistency() {
        verifyEnumConsistency(MessageProcessMap.class, MessageProcessMap.values());
        verifyEnumConsistency(IrcMessageValidationMap.class, IrcMessageValidationMap.values());
    }


    private <T extends Enum<T>> void verifyEnumConsistency(Class<T> type, T[] values) {
        List<T> list = Stream.of(values).collect(toList());

        for(MessageType messageType : MessageType.values()) {
            T process = EnumHelper.fromName(values, messageType.name())
                .orElseThrow(() -> new IllegalStateException(format("%s is missing MessageType %s", type.getSimpleName(), messageType.name())));
            list.remove(process);
        }

        if(!list.isEmpty())
            throw new IllegalStateException(format("%s contains extra entries not in MessageType: %s", type.getSimpleName(),
                String.join(", ", list.stream().map(Enum::name).collect(toList()))));
    }
}
