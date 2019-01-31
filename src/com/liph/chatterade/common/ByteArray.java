package com.liph.chatterade.common;

import java.util.Arrays;
import java.util.Base64;


public class ByteArray {

    private static Base64.Encoder base64encoder = Base64.getEncoder();
    private final byte[] bytes;


    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    public ByteArray(byte[] bytes, int length) {
        this.bytes = Arrays.copyOf(bytes, length);
    }


    public byte[] getBytes() {
        return bytes;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
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

        return Arrays.equals(bytes, other.bytes);
    }

    @Override
    public String toString() {
        return base64encoder.encodeToString(bytes);
    }
}
