package com.liph.chatterade.encryption.models;

import com.liph.chatterade.common.Base32Encoder;
import com.liph.chatterade.common.ByteArray;


public abstract class Key {

    public static final int BYTE_SIZE = 32;

    private final ByteArray keyBytes;


    public Key(ByteArray keyBytes) {
        this.keyBytes = keyBytes;
    }

    public Key(byte[] keyBytes) {
        this.keyBytes = new ByteArray(keyBytes);
    }

    public Key(String base32key) {
        this.keyBytes = new ByteArray(base32key);
    }


    public ByteArray getSigningKey() {
        return keyBytes;
    }

    public String getBase32SigningKey() {
        return keyBytes.toString();
    }


    public abstract byte[] getEncryptionKey();


    public String getBase32EncryptionKey() {
        return Base32Encoder.getBase32(getEncryptionKey());
    }
}
