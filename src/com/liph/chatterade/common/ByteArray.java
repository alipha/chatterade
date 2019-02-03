package com.liph.chatterade.common;

import com.liph.chatterade.encryption.EncryptionService;
import java.security.MessageDigest;
import java.util.Arrays;


public class ByteArray {

    private final byte[] bytes;
    private final int hashCode;


    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
        this.hashCode = EncryptionService.getInstance().getHashCode(this.bytes);
    }

    public ByteArray(byte[] bytes, int length) {
        this.bytes = Arrays.copyOf(bytes, length);
        this.hashCode = EncryptionService.getInstance().getHashCode(this.bytes);
    }


    public ByteArray(byte[] bytes, int offset, int length) {
        this.bytes = Arrays.copyOfRange(bytes, offset, offset + length);
        this.hashCode = EncryptionService.getInstance().getHashCode(this.bytes);
    }

    public ByteArray(String base32) {
        this.bytes = Base32Encoder.getBytes(base32);
        this.hashCode = EncryptionService.getInstance().getHashCode(this.bytes);
    }


    public byte[] getBytes() {
        return bytes;
    }


    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ByteArray other = (ByteArray) obj;

        return MessageDigest.isEqual(bytes, other.bytes);
    }

    @Override
    public String toString() {
        return Base32Encoder.getBase32(bytes);
    }
}
