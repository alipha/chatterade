package com.liph.chatterade.encryption.enums;

import static java.lang.String.format;


public enum ServerMessageType {
    INITIAL(0),
    NORMAL(1);

    private byte value;

    ServerMessageType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return this.value;
    }


    public static ServerMessageType fromValue(byte value) {

        for(ServerMessageType type : values())
            if(type.getValue() == value)
                return type;

        throw new IllegalArgumentException(format("%d is not a valid ServerMessageType", (int)value));
    }
}
