package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.messaging.enums.MessageActionMap;
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

        while(true) {
            DecryptedMessage message = readMessage();
            Message parsedMessage = ircParser.parse(message.getMessage(), false);

            Optional<Contact> sender = ircParser.parseSender(parsedMessage.getTokenizedMessage().getSenderName(), message.getSenderPublicKey());
            sender.ifPresent(s -> MessageActionMap.process(application.getServerMessageProcessor(), parsedMessage, s, message.getRecipient()));
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
        synchronized (writer) {
            try {
                writer.writeShort(message.length);
                writer.write(message);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private DecryptedMessage readMessage() throws IOException {
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
                    return message.get();

            } catch (MalformedIrcMessageException e) {
                e.printStackTrace();
            }
        }
    }
}
