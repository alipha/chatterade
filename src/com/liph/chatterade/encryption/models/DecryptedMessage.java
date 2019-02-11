package com.liph.chatterade.encryption.models;


import com.liph.chatterade.common.ByteArray;

public class DecryptedMessage {

    private final ByteArray recentMessageHash;
    private final ByteArray senderPublicKey;
    private final PublicKey targetPublicKey;
    private final String message;


    public DecryptedMessage(ByteArray recentMessageHash, ByteArray senderPublicKey, PublicKey targetPublicKey, String message) {
        this.recentMessageHash = recentMessageHash;
        this.senderPublicKey = senderPublicKey;
        this.targetPublicKey = targetPublicKey;
        this.message = message;
    }


    public ByteArray getRecentMessageHash() {
        return recentMessageHash;
    }

    public ByteArray getSenderPublicKey() {
        return senderPublicKey;
    }

    public PublicKey getTargetPublicKey() {
        return targetPublicKey;
    }

    public String getMessage() {
        return message;
    }
}
