package com.liph.chatterade;

import com.liph.chatterade.chat.Application;

public class Main {

    public static void main(String[] args) {
        int clientPort = 6667;
        int serverPort = 6668;

        if(args.length >= 1)
            clientPort = Integer.parseInt(args[0]);

        if(args.length >= 2)
            serverPort = Integer.parseInt(args[1]);

        Application application = new Application("alipha.ddns.net", "0.1", clientPort, serverPort);
        application.run();
    }
}
