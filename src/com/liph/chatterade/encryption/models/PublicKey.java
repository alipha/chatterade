package com.liph.chatterade.encryption.models;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.EncryptionService;


public class PublicKey extends Key {

    public PublicKey(ByteArray keyBytes) {
        super(keyBytes);
    }

    public PublicKey(byte[] keyBytes) {
        super(keyBytes);
    }

    public PublicKey(String base32key) {
        super(base32key);
    }


    @Override
    public byte[] getEncryptionKey() {
        return EncryptionService.getInstance().getEncryptionPublicKey(getSigningKey());
    }
}
