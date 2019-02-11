package com.liph.chatterade.encryption.models;

import com.muquit.libsodiumjna.SodiumKeyPair;


public class KeyPair {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;


    public KeyPair(SodiumKeyPair keyPair) {
        this.publicKey = new PublicKey(keyPair.getPublicKey());
        this.privateKey = new PrivateKey(keyPair.getPrivateKey());
    }

    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }


    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
