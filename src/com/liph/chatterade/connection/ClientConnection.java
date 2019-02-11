package com.liph.chatterade.connection;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.messaging.enums.MessageActionMap;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.connection.exceptions.ConnectionClosedException;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.messaging.models.NickMessage;
import com.liph.chatterade.messaging.models.PassMessage;
import com.liph.chatterade.messaging.models.UserMessage;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.exceptions.MalformedIrcMessageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;


public class ClientConnection extends Connection {

    private final IrcParser ircParser;
    private BufferedReader reader;
    private PrintWriter writer;

    private Optional<ClientUser> clientUser = Optional.empty();


    public ClientConnection(Application application, Socket socket) {
        super(application, socket);
        this.ircParser = new IrcParser();
    }

    @Override
    public void doRun() throws Exception {
        reader = new BufferedReader(new InputStreamReader(input));
        writer = new PrintWriter(output);


        Optional<String> serverPass = Optional.empty();

        // read PASS message, if sent
        Message message = readMessage();

        if(message.getType() == MessageType.PASS) {
            serverPass = Optional.of(((PassMessage)message).getPassword());
        }

        // read until NICK message is sent
        while(message.getType() != MessageType.NICK) {
            message = readMessage();
        }
        String nick = ((NickMessage)message).getNewNick();


        KeyPair keyPair = EncryptionService.getInstance().generateKeyPair();
        ClientUser user = new ClientUser(nick, keyPair, this);
        clientUser = Optional.of(user);

        // read until USER message is sent
        while(message.getType() != MessageType.USER) {
            message = readMessage();
        }
        UserMessage userMessage = (UserMessage)message;
        user.setUsername(Optional.of(userMessage.getUsername()));
        user.setRealName(Optional.of(userMessage.getRealName()));

        application.getClientUserManager().addUser(user);

        // user is logged in, now perform message loop
        while(true) {
            message = readMessage();
            message.setSender(user);
            MessageActionMap.process(application.getClientMessageProcessor(), message);
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

        clientUser.ifPresent(u -> application.getClientUserManager().removeUser(u));
    }


    public void sendMessage(String message) {
        System.out.println(format("%s <- %s", clientUser.flatMap(User::getNick).orElse("unknown"), message));
        synchronized (writer) {
            writer.write(message);
            writer.write("\r\n");
            writer.flush();
        }
    }

    public void sendMessage(String sender, String messageType, String message) {
        String nick = clientUser.flatMap(ClientUser::getNick).orElse("*");
        sendMessage(format(":%s %s %s %s", sender, messageType, nick, message));
    }


    private Message readMessage() throws IOException {
        while(true) {
            try {
                String line;
                do {
                    line = reader.readLine();
                    if(line == null)
                        throw new ConnectionClosedException();
                    System.out.println(format("%s -> %s", clientUser.flatMap(User::getNick).orElse("unknown"), line));
                } while(line.trim().equals(""));

                return ircParser.parse(line, true);
            } catch (MalformedIrcMessageException e) {
                sendMessage(application.getServerName(), e.getErrorCode(), e.getMessage());
            }
        }
    }
}
