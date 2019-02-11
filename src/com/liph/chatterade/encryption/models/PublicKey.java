package com.liph.chatterade.encryption.models;

import com.liph.chatterade.encryption.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;


public class PublicKey extends Key {

    public PublicKey(byte[] keyBytes) {
        super(keyBytes);
    }

    public PublicKey(String base32key) {
        super(base32key);
    }


    @Override
    public byte[] getEncryptionKey() {
        try {
            return SodiumLibrary.cryptoSignEdPkTOcurvePk(getSigningKey().getBytes());
        } catch(SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }
}
