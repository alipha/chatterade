package com.liph.chatterade.encryption.models;

import com.liph.chatterade.common.Base32Encoder;
import com.liph.chatterade.common.ByteArray;
import com.muquit.libsodiumjna.SodiumKeyPair;
import com.liph.chatterade.encryption.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import java.util.Optional;


public class Key {

    public static final int BYTE_SIZE = 32;


    private final ByteArray publicKey;
    private final Optional<byte[]> privateKey;


    public Key(byte[] publicKey) {
        this.publicKey = new ByteArray(publicKey);
        this.privateKey = Optional.empty();
    }

    public Key(byte[] publicKey, byte[] privateKey) {
        this.publicKey = new ByteArray(publicKey);
        this.privateKey = Optional.of(privateKey);
    }

    public Key(String publicKey) {
        this.publicKey = new ByteArray(publicKey);
        this.privateKey = Optional.empty();
    }

    public Key(String publicKey, String privateKey) {
        this.publicKey = new ByteArray(publicKey);
        this.privateKey = Optional.of(Base32Encoder.getBytes(privateKey));
    }

    public Key(SodiumKeyPair keyPair) {
        this.publicKey = new ByteArray(keyPair.getPublicKey());
        this.privateKey = Optional.of(keyPair.getPrivateKey());
    }


    public ByteArray getSigningPublicKey() {
        return publicKey;
    }

    public Optional<byte[]> getSigningPrivateKey() {
        return privateKey;
    }

    public String getBase32SigningPublicKey() {
        return publicKey.toString();
    }

    public Optional<String> getBase32SigningPrivateKey() {
        return privateKey.map(Base32Encoder::getBase32);
    }

    public byte[] getDHPublicKey() {
        try {
            return SodiumLibrary.cryptoSignEdPkTOcurvePk(publicKey.getBytes());
        } catch(SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<byte[]> getDHPrivateKey() {
        return privateKey.map(sk -> {
            try {
                return SodiumLibrary.cryptoSignEdSkTOcurveSk(sk);
            } catch (SodiumLibraryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String getBase32DHPublicKey() {
        return Base32Encoder.getBase32(getDHPublicKey());
    }

    public Optional<String> getBase32DHPrivateKey() {
        return getDHPrivateKey().map(Base32Encoder::getBase32);
    }
}
