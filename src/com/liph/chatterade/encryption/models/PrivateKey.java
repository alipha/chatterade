package com.liph.chatterade.encryption.models;

import com.liph.chatterade.encryption.EncryptionService;


public class PrivateKey extends Key {

    public PrivateKey(byte[] keyBytes) {
        super(keyBytes);
    }

    public PrivateKey(String base32key) {
        super(base32key);
    }


    @Override
    public byte[] getEncryptionKey() {
        return EncryptionService.getInstance().getEncryptionPrivateKey(getSigningKey());
    }
}
