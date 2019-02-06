package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.enums.MessageProcessMap;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.exceptions.MalformedIrcMessageException;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static java.lang.String.format;

public class ServerConnection extends Connection {

    private final IrcParser ircParser;
    private DataInputStream reader;
    private DataOutputStream writer;

    private final String remoteAddress;


    public ServerConnection(Application application, Socket socket) {
        super(application, socket);
        this.ircParser = new IrcParser();
        this.remoteAddress = format("%s:%s", socket.getInetAddress().getHostAddress(), socket.getPort());
    }


    @Override
    public void doRun() throws Exception {
        reader = new DataInputStream(input);
        writer = new DataOutputStream(output);

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


    public void sendMessage(byte[] message) {
        try {
            writer.writeShort(message.length);
            writer.write(message);
            writer.flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    private Message readMessage() throws IOException {
        while(true) {
            try {
                short length = reader.readShort();

                byte[] encryptedMessage = new byte[length];
                reader.readFully(encryptedMessage);

                if(!application.relayMessage(encryptedMessage, Optional.of(this)))
                    continue;

                Optional<DecryptedMessage> message = application.decryptMessage(encryptedMessage);
                // TODO: validate recent message hash before processing

                if(message.isPresent())
                    return ircParser.parse(message.get().getMessage(), false);
            } catch (MalformedIrcMessageException e) {
                e.printStackTrace();
            }
        }
    }
}
