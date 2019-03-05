package com.liph.chatterade.encryption.models;


public class SodiumKxKeyPair {
    private byte[] receivingKey;
    private byte[] transmittingKey;


    public SodiumKxKeyPair(byte[] receivingKey, byte[] transmittingKey) {
        this.receivingKey = receivingKey;
        this.transmittingKey = transmittingKey;
    }

    public byte[] getReceivingKey() {
        return this.receivingKey;
    }

    public byte[] getTransmittingKey() {
        return this.transmittingKey;
    }
}
