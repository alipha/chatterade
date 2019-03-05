package com.liph.chatterade.connection;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.encryption.EncryptedTunnel;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.SodiumLibrary;
import com.liph.chatterade.encryption.models.SodiumKxKeyPair;
import com.liph.chatterade.messaging.enums.MessageActionMap;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.exceptions.MalformedIrcMessageException;

import com.muquit.libsodiumjna.SodiumKeyPair;
import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static java.lang.String.format;


public class ServerConnection extends Connection {

    private final IrcParser ircParser;
    private final String remoteAddress;
    private final boolean isInitiator;
    private EncryptedTunnel tunnel;


    public ServerConnection(Application application, Socket socket) {
        this(application, socket, false);
    }

    public ServerConnection(Application application, Socket socket, boolean isInitiator) {
        super(application, socket);
        this.ircParser = new IrcParser();
        this.remoteAddress = format("%s:%s", socket.getInetAddress().getHostAddress(), socket.getPort());
        this.isInitiator = isInitiator;
    }


    @Override
    public void doRun() throws Exception {
        tunnel = new EncryptedTunnel(isInitiator, input, output);
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
        tunnel.close();
        application.removeServer(this);
    }


    public void sendMessage(byte[] message) {
        tunnel.send(message);
    }


    private DecryptedMessage readMessage() {
        while(true) {
            try {
                byte[] encryptedMessage = tunnel.read();

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
