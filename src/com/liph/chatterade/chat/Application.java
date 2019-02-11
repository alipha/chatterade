package com.liph.chatterade.chat;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.liph.chatterade.encryption.models.PublicKey;
import com.liph.chatterade.messaging.ClientMessageProcessor;
import com.liph.chatterade.messaging.RecentMessageManager;
import com.liph.chatterade.messaging.ServerMessageProcessor;
import com.liph.chatterade.messaging.enums.MessageActionMap;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.connection.ConnectionListener;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.connection.models.RecentMessage;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.IrcFormatter;
import com.liph.chatterade.parsing.enums.IrcMessageValidationMap;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class Application {

    private final Instant startupTime;
    private final String serverName;
    private final String serverVersion;

    private final EncryptionService encryptionService;
    private final IrcFormatter ircFormatter;
    private final Set<ServerConnection> serverConnections;

    private RecentMessageManager recentMessageManager;
    private ClientUserManager clientUserManager;
    private ClientMessageProcessor clientMessageProcessor;
    private ServerMessageProcessor serverMessageProcessor;


    public Application(String serverName, String serverVersion, EncryptionService encryptionService, IrcFormatter ircFormatter) {
        this.startupTime = Instant.now();
        this.serverName = serverName;
        this.serverVersion = serverVersion;

        this.encryptionService = encryptionService;
        this.ircFormatter = ircFormatter;
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

    public IrcFormatter getIrcFormatter() {
        return ircFormatter;
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
            Optional<DecryptedMessage> decryptedMessage = encryptionService.decryptMessage(user.getKeyPair(), encryptedMessage);
            if(decryptedMessage.isPresent())
                return decryptedMessage;
        }

        return Optional.empty();
    }


    public void sendNickChange(ClientUser user, String previousNick, PublicKey publicKey, String newNick) {
        String sender = ircFormatter.getFullyQualifiedName(previousNick, "unknown", publicKey);
        String message = ircFormatter.formatMessage(sender, "NICK", format(":%s", newNick));
        user.getConnection().sendMessage(message);
    }


    private void verifyCodeConsistency() {
        verifyEnumConsistency(MessageActionMap.class, MessageActionMap.values());
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
