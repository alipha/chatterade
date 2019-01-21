package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ConnectionListener implements Runnable {

    private final Application application;
    private final int port;
    private final BiFunction<Application, Socket, Connection> acceptAction;
    private final List<Connection> connections;

    private ServerSocket listenSocket;
    private volatile boolean running;


    public ConnectionListener(Application application, int port, BiFunction<Application, Socket, Connection> acceptAction) {
        this.application = application;
        this.port = port;
        this.acceptAction = acceptAction;
        this.connections = new ArrayList<>();
    }


    public List<Connection> getConnections() {
        return connections;
    }


    @Override
    public void run() {
        try {
            listenSocket = new ServerSocket(this.port);
        } catch (IOException e) {
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
}