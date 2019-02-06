package com.liph.chatterade.connection.models;


import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.connection.ServerConnection;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class RecentMessage {

    private final ByteArray hash;
    private final Instant timestamp;
    private final Set<ServerConnection> originators;


    public RecentMessage(ByteArray hash) {
        this.hash = hash;
        this.timestamp = Instant.now();
        this.originators = ConcurrentHashMap.newKeySet();
    }


    public ByteArray getHash() {
        return hash;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Set<ServerConnection> getOriginators() {
        return originators;
    }

    public long getAgeMs() {
        return Instant.now().toEpochMilli() - timestamp.toEpochMilli();
    }
}
