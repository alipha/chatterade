package com.liph.chatterade.encryption.models;

import com.liph.chatterade.encryption.enums.ServerMessageType;

public class TunneledMessage {

    private final ServerMessageType type;
    private final byte[] message;

    public TunneledMessage(ServerMessageType type, byte[] message) {
        this.type = type;
        this.message = message;
    }


    public ServerMessageType getType() {
        return type;
    }

    public byte[] getMessage() {
        return message;
    }
}
