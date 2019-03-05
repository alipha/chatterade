package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public abstract class Connection implements Runnable, AutoCloseable {

    protected final Application application;
    protected final Socket socket;

    protected InputStream input;
    protected OutputStream output;


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

            doRun();

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    @Override
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
    }
}
