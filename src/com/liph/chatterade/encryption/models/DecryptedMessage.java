package com.liph.chatterade.encryption.models;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.common.ByteArray;


public class DecryptedMessage {

    private final ByteArray recentMessageHash;
    private final ByteArray senderPublicKey;
    private final ClientUser recipient;
    private final String message;


    public DecryptedMessage(ByteArray recentMessageHash, ByteArray senderPublicKey, ClientUser recipient, String message) {
        this.recentMessageHash = recentMessageHash;
        this.senderPublicKey = senderPublicKey;
        this.recipient = recipient;
        this.message = message;
    }


    public ByteArray getRecentMessageHash() {
        return recentMessageHash;
    }

    public ByteArray getSenderPublicKey() {
        return senderPublicKey;
    }

    public ClientUser getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }
}
