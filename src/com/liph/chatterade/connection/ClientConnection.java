package com.liph.chatterade.connection;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.enums.MessageProcessMap;
import com.liph.chatterade.connection.exceptions.ConnectionClosedException;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.messaging.models.NickMessage;
import com.liph.chatterade.messaging.models.PassMessage;
import com.liph.chatterade.messaging.models.UserMessage;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.exceptions.MalformedIrcMessageException;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;


public class ClientConnection extends Connection {

    private final IrcParser ircParser;

    private ClientUser user;


    public ClientConnection(Application application, Socket socket) {
        super(application, socket);
        this.ircParser = new IrcParser();
    }

    @Override
    public void doRun() throws Exception {
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

        // read until USER message is sent
        while(message.getType() != MessageType.USER) {
            message = readMessage();
        }
        UserMessage userMessage = (UserMessage)message;

        // user is logged in, now perform message loop
        ClientUser user = application.addUser(nick, userMessage.getUsername(), userMessage.getRealName(), serverPass, this);

        while(true) {
            message = readMessage();
            message.setSender(user);
            MessageProcessMap.process(application, message);
        }
    }

    @Override
    public void doClose() throws Exception {
        application.removeUser(user);
    }


    public void sendMessage(String sender, String messageType, String message) {
        writer.write(format(":%s %s %s %s\r\n", sender, messageType, user.getNick(), message));
    }


    private Message readMessage() throws IOException {
        while(true) {
            try {
                String line;
                do {
                    line = reader.readLine();
                    if(line == null)
                        throw new ConnectionClosedException();
                } while(line.trim().equals(""));

                return ircParser.parse(line, true);
            } catch (MalformedIrcMessageException e) {
                sendMessage(application.getServerName(), e.getErrorCode(), e.getMessage());
            }
        }
    }
}
