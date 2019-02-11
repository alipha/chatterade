package com.liph.chatterade.connection;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;


public class ConnectionListener implements Runnable {

    private final Application application;
    private final int port;
    private final BiFunction<Application, Socket, Connection> acceptAction;
    private final List<Connection> connections;
    private final boolean useTls;

    private ServerSocket listenSocket;
    private volatile boolean running;


    public ConnectionListener(Application application, int port, BiFunction<Application, Socket, Connection> acceptAction, boolean useTls) {
        this.application = application;
        this.port = port;
        this.acceptAction = acceptAction;
        this.connections = new ArrayList<>();
        this.useTls = useTls;
    }


    public List<Connection> getConnections() {
        return connections;
    }


    @Override
    public void run() {
        try {
            listenSocket = getServerSocketFactory(useTls).createServerSocket(this.port);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        running = true;

        while(running) {
            Socket socket;
            try {
                socket = listenSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            Connection connection = acceptAction.apply(application, socket);
            new Thread(connection).start();
            connections.add(connection);
        }
    }


    public void close() {
        for(Connection connection : connections) {
            connection.close();
        }

        running = false;

        try {
            listenSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ServerSocketFactory getServerSocketFactory(boolean useTls) throws Exception {
        if (useTls) {
            SSLServerSocketFactory ssf;
            SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;
            char[] passphrase = "passphrase".toCharArray();

            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");

            String userHomeDir = System.getProperty("user.home");

            try(FileInputStream keystoreFile = new FileInputStream(format("%s/.chatterade/keystore.ks", userHomeDir))) {
                ks.load(keystoreFile, passphrase);
                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);

                ssf = ctx.getServerSocketFactory();
            }
            return ssf;
        } else {
            return ServerSocketFactory.getDefault();
        }
    }
}
