package com.liph.chatterade.encryption.models;

import com.liph.chatterade.common.ByteArray;
import com.muquit.libsodiumjna.SodiumKeyPair;
import com.muquit.libsodiumjna.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import java.util.Base64;
import java.util.Optional;


public class Key {

    public static final int BYTE_SIZE = 32;

    private static Base64.Encoder base64encoder = Base64.getEncoder();
    private static Base64.Decoder base64decoder = Base64.getDecoder();

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
        this.publicKey = new ByteArray(base64decoder.decode(publicKey));
        this.privateKey = Optional.empty();
    }

    public Key(String publicKey, String privateKey) {
        this.publicKey = new ByteArray(base64decoder.decode(publicKey));
        this.privateKey = Optional.of(base64decoder.decode(privateKey));
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

    public String getBase64SigningPublicKey() {
        return base64encoder.encodeToString(publicKey.getBytes());
    }

    public Optional<String> getBase64SigningPrivateKey() {
        return privateKey.map(k -> base64encoder.encodeToString(k));
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

    public String getBase64DHPublicKey() {
        return base64encoder.encodeToString(getDHPublicKey());
    }

    public Optional<String> getBase64DHPrivateKey() {
        return getDHPrivateKey().map(k -> base64encoder.encodeToString(k));
    }
}
