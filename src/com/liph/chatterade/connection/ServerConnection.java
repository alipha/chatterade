package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection extends Connection {


    public ServerConnection(Application application, Socket socket) {
        super(application, socket);
    }

    @Override
    public void doRun() throws Exception {

    }

    @Override
    public void doClose() throws Exception {

    }
}
