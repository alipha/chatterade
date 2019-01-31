package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.enums.MessageProcessMap;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.connection.exceptions.ConnectionClosedException;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.exceptions.MalformedIrcMessageException;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static java.lang.String.format;

public class ServerConnection extends Connection {

    private final IrcParser ircParser;
    private BufferedReader reader;
    private PrintWriter writer;
    //private DataInputStream reader;
    //private DataOutputStream writer;

    private final String remoteAddress;


    public ServerConnection(Application application, Socket socket) {
        super(application, socket);
        this.ircParser = new IrcParser();
        this.remoteAddress = format("%s:%s", socket.getInetAddress().getHostAddress(), socket.getPort());
    }


    @Override
    public void doRun() throws Exception {
        //reader = new DataInputStream(input);
        //writer = new DataOutputStream(output);
        reader = new BufferedReader(new InputStreamReader(input));
        writer = new PrintWriter(output);

        application.addServerConnection(this);

        Message message;

        while(true) {
            message = readMessage();
            message.setSender(ircParser.parseSender(message.getTokenizedMessage().getSenderName()).get());  // TODO: sender for server messages?
            MessageProcessMap.process(application, message);
            // TODO: add logic to pass along message to other servers
        }
    }

    @Override
    public void doClose() throws Exception {
        try {
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        application.removeServer(this);
    }


    public void sendMessage(String message) {
        writer.write(message);
        writer.write("\r\n");
        writer.flush();
    }


    private Message readMessage() throws IOException {
        while(true) {
            try {
                String line;
                do {
                    line = reader.readLine();
                    if(line == null)
                        throw new ConnectionClosedException();
                    System.out.println(format("%s -> %s", remoteAddress, line));
                    // TODO: relay the *encrypted* message
                } while(line.trim().equals("") || !application.relayMessage(line, Optional.of(this)));

                return ircParser.parse(line, false);
            } catch (MalformedIrcMessageException e) {
                //sendMessage(application.getServerName(), e.getErrorCode(), e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
