package com.liph.chatterade;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.messaging.ClientMessageProcessor;
import com.liph.chatterade.chat.ClientUserManager;
import com.liph.chatterade.messaging.RecentMessageManager;
import com.liph.chatterade.messaging.ServerMessageProcessor;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.connection.ConnectionListener;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.Key;
import com.muquit.libsodiumjna.SodiumKeyPair;
import com.liph.chatterade.encryption.SodiumLibrary;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int clientPort = 6667;
        int serverPort = 6668;
        int clientTlsPort = 7000;

        if(args.length >= 1)
            clientPort = Integer.parseInt(args[0]);

        if(args.length >= 2)
            serverPort = Integer.parseInt(args[1]);

        if(args.length >= 3)
            clientTlsPort = Integer.parseInt(args[2]);

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

        Application application = new Application("alipha.ddns.net", "0.1", EncryptionService.getInstance());

        List<ConnectionListener> connectionListeners = Arrays.asList(
            new ConnectionListener(application, clientPort, ClientConnection::new, false),
            new ConnectionListener(application, serverPort, ServerConnection::new, false),
            new ConnectionListener(application, clientTlsPort, ClientConnection::new, true)
        );

        application.run(
            new RecentMessageManager(),
            new ClientUserManager(application),
            new ClientMessageProcessor(application),
            new ServerMessageProcessor(application),
            connectionListeners);
    }


    public static void timing() throws SodiumLibraryException {
        EncryptionService encryptionService = EncryptionService.getInstance();

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


        KeyPair key2 = new KeyPair(key);
        byte[] salt = {32, 89, -42, 120};
        start = System.currentTimeMillis();
        for(int i = 0; i < 200000; i++) {
            encryptionService.getShortPublicKeyHash(salt, key2.getPublicKey());
        }
        end = System.currentTimeMillis();

        countPerSec = 200000*1000 / (end - start);
        System.out.println(format(" Short hashes: %d/sec", countPerSec));


    }
}
