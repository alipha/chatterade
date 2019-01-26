package com.liph.chatterade.encryption;

import com.liph.chatterade.encryption.models.Key;
import com.muquit.libsodiumjna.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import com.sun.jna.Platform;


public class EncryptionService {

    private static boolean isInitialized = false;


    public EncryptionService() {
        if(!isInitialized) {
            isInitialized = true;
            initialize();
        }
    }


    public Key generateKey() {
        try {
            return new Key(SodiumLibrary.cryptoSignKeyPair());
        } catch(SodiumLibraryException e) {
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
}
