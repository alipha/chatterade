package com.liph.chatterade.connection.models;


import com.liph.chatterade.common.ByteArray;
import java.time.Instant;


public class RecentMessage {

    private final ByteArray hash;
    private final Instant timestamp;


    public RecentMessage(ByteArray hash) {
        this.hash = hash;
        this.timestamp = Instant.now();
    }


    public ByteArray getHash() {
        return hash;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public long getAgeMs() {
        return Instant.now().toEpochMilli() - timestamp.toEpochMilli();
    }
}
