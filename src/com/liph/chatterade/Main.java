package com.liph.chatterade;

import com.liph.chatterade.chat.Application;
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
}
