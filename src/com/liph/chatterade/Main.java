package com.liph.chatterade;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.Key;
import com.muquit.libsodiumjna.SodiumKeyPair;
import com.muquit.libsodiumjna.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        int clientPort = 6667;
        int serverPort = 6668;

        if(args.length >= 1)
            clientPort = Integer.parseInt(args[0]);

        if(args.length >= 2)
            serverPort = Integer.parseInt(args[1]);

        /*
        try {
            timing();
        } catch(SodiumLibraryException e) {
            e.printStackTrace();
            return;
        }
        */

        /*
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int clientPort;
        try {
            clientPort = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int serverPort = clientPort + 1;
        */

        Application application = new Application("alipha.ddns.net", "0.1", clientPort, serverPort);
        application.run();
    }


    public static void timing() throws SodiumLibraryException {
        EncryptionService encryptionService = new EncryptionService();

        SodiumKeyPair key = SodiumLibrary.cryptoBoxKeyPair();
        byte[] ciphertext = SodiumLibrary.cryptoBoxSeal("test".getBytes(), key.getPublicKey());

        long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++)
            SodiumLibrary.cryptoBoxSealOpen(ciphertext, key.getPublicKey(), key.getPrivateKey());
        long end = System.currentTimeMillis();

        long countPerSec = 10000*1000 / (end - start);
        System.out.println(format("Successful decrypts: %d/sec", countPerSec));


        ciphertext[2] = 0;
        start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++) {
            try {
                SodiumLibrary.cryptoBoxSealOpen(ciphertext, key.getPublicKey(), key.getPrivateKey());
                System.out.println("Expected exception");
            } catch(SodiumLibraryException e) {}
        }
        end = System.currentTimeMillis();

        countPerSec = 10000*1000 / (end - start);
        System.out.println(format("Failed decrypts: %d/sec", countPerSec));


        Key key2 = new Key(key);
        byte[] salt = {32, 89, -42, 120};
        start = System.currentTimeMillis();
        for(int i = 0; i < 200000; i++) {
            encryptionService.getShortPublicKeyHash(salt, key2);
        }
        end = System.currentTimeMillis();

        countPerSec = 200000*1000 / (end - start);
        System.out.println(format(" Short hashes: %d/sec", countPerSec));


    }
}
