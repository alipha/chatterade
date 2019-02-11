package com.liph.chatterade.encryption.models;


import com.liph.chatterade.encryption.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;


public class PrivateKey extends Key {

    public PrivateKey(byte[] keyBytes) {
        super(keyBytes);
    }

    public PrivateKey(String base32key) {
        super(base32key);
    }


    @Override
    public byte[] getEncryptionKey() {
        try {
            return SodiumLibrary.cryptoSignEdSkTOcurveSk(getSigningKey().getBytes());
        } catch(SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }

}
