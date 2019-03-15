package com.liph.chatterade;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.common.LockManager;
import com.liph.chatterade.messaging.ClientMessageProcessor;
import com.liph.chatterade.chat.ClientUserManager;
import com.liph.chatterade.messaging.RecentMessageManager;
import com.liph.chatterade.messaging.ServerMessageProcessor;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.connection.ConnectionListener;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.parsing.IrcFormatter;
import com.liph.chatterade.serialization.Serializer;
import java.util.Arrays;
import java.util.List;


public class Main {

    public static void main2(String[] args) {
        Playground.hashMapTest();
        //iniTest();
    }

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

        LockManager lockManager = new LockManager();
        Serializer serializer = new Serializer(lockManager);

        Application application = new Application("alipha.ddns.net", "0.1",
                EncryptionService.getInstance(), new IrcFormatter(), serializer, lockManager);

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
}
