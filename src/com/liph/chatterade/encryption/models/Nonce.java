package com.liph.chatterade.encryption.models;


import com.liph.chatterade.encryption.EncryptionService;
import java.security.MessageDigest;
import java.util.Arrays;


public class Nonce implements Comparable<Nonce> {

    private byte[] bytes;


    public static Nonce random() {
        return new Nonce(EncryptionService.getInstance().randomNonce());
    }

    public Nonce() {
        this.bytes = new byte[EncryptionService.NONCE_SIZE];
    }

    public Nonce(byte[] bytes) {
        this.bytes = bytes;
    }


    public byte[] getBytes() {
        return bytes;
    }


    public void increment() {
        for(int i = bytes.length - 1; i >= 0; i--) {
            if(bytes[i] == 127)
                bytes[i] = -128;
            else
                bytes[i]++;

            if(bytes[i] != 0)
                return;
        }

        throw new RuntimeException("Nonce rolled over.");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Nonce nonce = (Nonce) o;

        return MessageDigest.isEqual(bytes, nonce.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }


    @Override
    public int compareTo(Nonce other) {
        for(int i = 0; i < bytes.length; i++) {
            if(this.bytes[i] != other.bytes[i]) {
                return (this.bytes[i] & 0xff) - (other.bytes[i] & 0xff);
            }
        }
        return 0;
    }
}
