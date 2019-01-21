package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Connection implements Runnable {

    protected final Application application;
    protected final Socket socket;

    protected InputStream input;
    protected OutputStream output;
    protected BufferedReader reader;
    protected PrintWriter writer;


    protected Connection(Application application, Socket socket) {
        this.application = application;
        this.socket = socket;
    }


    protected abstract void doRun() throws Exception;

    protected abstract void doClose() throws Exception;


    @Override
    public void run() {
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            writer = new PrintWriter(output);

            doRun();

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    public void close() {
        try {
            doClose();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            input.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            output.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        writer.close();
    };
}