package com.liph.chatterade.encryption.models;

import com.muquit.libsodiumjna.SodiumKeyPair;
import com.muquit.libsodiumjna.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import java.util.Base64;
import java.util.Optional;


public class Key {

    private static Base64.Encoder base64encoder = Base64.getEncoder();
    private static Base64.Decoder base64decoder = Base64.getDecoder();

    private final byte[] publicKey;
    private final Optional<byte[]> privateKey;


    public Key(byte[] publicKey) {
        this.publicKey = publicKey;
        this.privateKey = Optional.empty();
    }

    public Key(byte[] publicKey, byte[] privateKey) {
        this.publicKey = publicKey;
        this.privateKey = Optional.of(privateKey);
    }

    public Key(String publicKey) {
        this.publicKey = base64decoder.decode(publicKey);
        this.privateKey = Optional.empty();
    }

    public Key(String publicKey, String privateKey) {
        this.publicKey = base64decoder.decode(publicKey);
        this.privateKey = Optional.of(base64decoder.decode(privateKey));
    }

    public Key(SodiumKeyPair keyPair) {
        this.publicKey = keyPair.getPublicKey();
        this.privateKey = Optional.of(keyPair.getPrivateKey());
    }


    public byte[] getSigningPublicKey() {
        return publicKey;
    }

    public Optional<byte[]> getSigningPrivateKey() {
        return privateKey;
    }

    public String getBase64SigningPublicKey() {
        return base64encoder.encodeToString(publicKey);
    }

    public Optional<String> getBase64SigningPrivateKey() {
        return privateKey.map(k -> base64encoder.encodeToString(k));
    }

    public byte[] getDHPublicKey() {
        try {
            return SodiumLibrary.cryptoSignEdPkTOcurvePk(publicKey);
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
