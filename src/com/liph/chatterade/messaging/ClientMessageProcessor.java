package com.liph.chatterade.messaging;


import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.User;
import com.liph.chatterade.connection.ServerConnection;
import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.models.ConnectMessage;
import com.liph.chatterade.messaging.models.JoinMessage;
import com.liph.chatterade.messaging.models.NickMessage;
import com.liph.chatterade.messaging.models.NoticeMessage;
import com.liph.chatterade.messaging.models.PartMessage;
import com.liph.chatterade.messaging.models.PassMessage;
import com.liph.chatterade.messaging.models.PingMessage;
import com.liph.chatterade.messaging.models.PongMessage;
import com.liph.chatterade.messaging.models.PrivateMessage;
import com.liph.chatterade.messaging.models.QuitMessage;
import com.liph.chatterade.messaging.models.UserMessage;
import java.net.Socket;
import java.util.Optional;


public class ClientMessageProcessor implements MessageProcessor {

    private final Application application;


    public ClientMessageProcessor(Application application) {
        this.application = application;
    }


    public void processJoin(JoinMessage message) {
    /*    ClientUser user = message.getSender();

        for(Entry<String, Optional<String>> channelKey : message.getChannelKeyMap().entrySet()) {
            Channel channel = new Channel(channelKey.getKey(), channelKey.getValue());
            if(!channels.add(channel))
                channel = channels;

        }
        */
    }

    public void processNick(NickMessage message) {
        message.getSender().setNick(Optional.of(message.getNewNick()));
    }

    public void processNotice(NoticeMessage message) {

    }

    public void processPart(PartMessage message) {

    }

    public void processPass(PassMessage message) {

    }

    public void processPrivateMessage(PrivateMessage message) {
        // TODO: separate out the ServerMessageProcessor code from the ClientMessageProcessor code
        /*
        Pair<Optional<ClientUser>, Optional<String>> targetAndPreviousNick = resolveTargetClientUser(message.getTarget(), message.getSender());
        Optional<ClientUser> target = targetAndPreviousNick.getFirst();
        Optional<String> previousNick = targetAndPreviousNick.getSecond();
        */
        Optional<ClientUser> senderClientUser = Optional.empty();

        if(message.getSender() instanceof ClientUser) {
            senderClientUser = Optional.of((ClientUser)message.getSender());
        }

        Optional<User> targetOpt = application.getClientUserManager().resolveTargetUser(message.getTarget(), senderClientUser);

        if(targetOpt.isPresent()) {
            User target = targetOpt.get();

            //previousNick.ifPresent(n -> message.getSender().getConnection().sendMessage(format(":%s NICK %s", n, target.get().getNick().get())));
            String targetNick = target.getNick().orElse(target.getPublicKey().get().getBase32SigningKey());

            if(!targetNick.equals(message.getTargetText())) {
                senderClientUser.ifPresent(u -> application.sendNickChange(u, message.getTargetText(), target));
            }

            if(target instanceof ClientUser) {
                ClientUser targetClientUser = (ClientUser)target;
                Optional<String> previousNick = targetClientUser.addOrUpdateContact(message.getSender());
                previousNick.ifPresent(previous -> application.sendNickChange(targetClientUser, previous, message.getSender()));

                targetClientUser.getConnection().sendMessage(message.getSender().getFullyQualifiedName(), MessageType.PRIVMSG.getIrcCommand(), format(":%s", message.getText()));
            } else {
                senderClientUser.ifPresent(u -> application.getClientUserManager().sendNetworkMessage(u, MessageType.PRIVMSG, target, format(":%s", message.getText())));
            }
        } else {
            senderClientUser.ifPresent(u -> u.getConnection().sendMessage(application.getServerName(), "401", format("%s :No such nick/channel", message.getTargetText())));
        }
    }

    public void processQuit(QuitMessage message) {

    }

    public void processUser(UserMessage message) {

    }

    public void processPing(PingMessage message) {
        ((ClientUser)message.getSender()).getConnection().sendMessage(application.getServerName(), MessageType.PONG.getIrcCommand(), message.getText());
    }

    public void processPong(PongMessage message) {

    }

    public void processConnect(ConnectMessage message) {
        try {
            Socket socket = new Socket(message.getServer(), message.getPort().orElse(6667));
            ServerConnection connection = new ServerConnection(application, socket);
            new Thread(connection).start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
