package com.liph.chatterade.encryption;

import com.muquit.libsodiumjna.SodiumLibrary;
import com.sun.jna.Platform;


public class EncryptionService {

    private static boolean isInitialized = false;


    public EncryptionService() {
        if(!isInitialized) {
            isInitialized = true;
            initialize();
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
