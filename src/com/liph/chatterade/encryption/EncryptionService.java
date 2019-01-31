package com.liph.chatterade.encryption;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.models.Key;
import com.muquit.libsodiumjna.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import com.sun.jna.Platform;


public class EncryptionService {

    private static boolean isInitialized = false;

    private final byte[] messageHashSalt;


    public EncryptionService() {
        if(!isInitialized) {
            isInitialized = true;
            initialize();
        }

        // add salt so the hash set organization is unpredictable and can't be DoS'd
        messageHashSalt = SodiumLibrary.randomBytes(16);
    }


    public Key generateKey() {
        try {
            return new Key(SodiumLibrary.cryptoSignKeyPair());
        } catch(SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public ByteArray getMessageHash(String message) {
        try {
            return new ByteArray(SodiumLibrary.cryptoGenerichash(concatArrays(messageHashSalt, message.getBytes()), 16));
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    private void initialize() {
        String libraryPath;

        if (Platform.isMac())
        {
            // MacOS
            libraryPath = "/usr/local/lib/libsodium.dylib";
        }
        else if (Platform.isWindows())
        {
            // Windows
            libraryPath = "./libsodium.dll";
        }
        else
        {
            // Linux
            libraryPath = "/usr/local/lib/libsodium.so";
        }

        SodiumLibrary.setLibraryPath(libraryPath);

        String v = SodiumLibrary.libsodiumVersionString();
        System.out.println("libsodium version: " + v);
    }


    private byte[] concatArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
