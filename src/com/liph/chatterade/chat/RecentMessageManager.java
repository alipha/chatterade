package com.liph.chatterade.chat;


import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.connection.models.RecentMessage;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class RecentMessageManager {

    private static final int RECENT_MESSAGE_SET_MAX_SIZE = 10000;
    private static final int RECENT_MESSAGE_MAX_AGE_MS = 1000 * 60;

    private final Map<ByteArray, RecentMessage> recentMessageSet;
    private final Deque<RecentMessage> recentMessageQueue;


    public RecentMessageManager() {
        this.recentMessageSet = new HashMap<>();
        this.recentMessageQueue = new LinkedList<>();
    }


    public boolean addToRecentMessages(RecentMessage message, Optional<ServerConnection> originator) {
        synchronized (recentMessageSet) {
            while(recentMessageQueue.size() > RECENT_MESSAGE_SET_MAX_SIZE || (!recentMessageQueue.isEmpty() && recentMessageQueue.peekLast().getAgeMs() > RECENT_MESSAGE_MAX_AGE_MS)) {
                RecentMessage evicted = recentMessageQueue.removeLast();
                recentMessageSet.remove(evicted.getHash());
            }

            RecentMessage existingMessage = recentMessageSet.get(message.getHash());
            if(existingMessage != null) {
                originator.ifPresent(o -> existingMessage.getOriginators().add(o));
                return false;
            }

            originator.ifPresent(o -> message.getOriginators().add(o));
            recentMessageSet.put(message.getHash(), message);
            recentMessageQueue.addFirst(message);
            return true;
        }
    }


    public ByteArray getMostRecentMessage() {
        synchronized (recentMessageSet) {
            if(recentMessageQueue.isEmpty()) {
                // TODO: exception?
                return new ByteArray(new byte[16]);
            } else {
                return recentMessageQueue.getFirst().getHash();
            }
        }
    }
}
