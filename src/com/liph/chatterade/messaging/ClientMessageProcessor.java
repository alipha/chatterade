package com.liph.chatterade.messaging;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.ResolveTargetResult;
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


public class ClientMessageProcessor {

    private final Application application;


    public ClientMessageProcessor(Application application) {
        this.application = application;
    }


    public void processJoin(JoinMessage message, ClientUser sender) {
    /*    ClientUser user = message.getSender();

        for(Entry<String, Optional<String>> channelKey : message.getChannelKeyMap().entrySet()) {
            Channel channel = new Channel(channelKey.getKey(), channelKey.getValue());
            if(!channels.add(channel))
                channel = channels;

        }
        */
    }

    public void processNick(NickMessage message, ClientUser sender) {
        sender.setNick(message.getNewNick());
        // TODO: send nick message
    }

    public void processNotice(NoticeMessage message, ClientUser sender) {

    }

    public void processPart(PartMessage message, ClientUser sender) {

    }

    public void processPass(PassMessage message, ClientUser sender) {

    }

    public void processPrivateMessage(PrivateMessage message, ClientUser sender) {
        // TODO: separate out the ServerMessageProcessor code from the ClientMessageProcessor code
        /*
        Pair<Optional<ClientUser>, Optional<String>> targetAndPreviousNick = resolveTargetClientUser(message.getTarget(), message.getSender());
        Optional<ClientUser> target = targetAndPreviousNick.getFirst();
        Optional<String> previousNick = targetAndPreviousNick.getSecond();
        */

        ResolveTargetResult result = application.getClientUserManager().resolveTargetUser(message.getTarget(), Optional.of(sender));

        if(result.getClientUser().isPresent()) {
            ClientUser target = result.getClientUser().get();

            //previousNick.ifPresent(n -> message.getSender().getConnection().sendMessage(format(":%s NICK %s", n, target.get().getNick().get())));
            if(!target.getNick().equals(message.getTargetText())) {
                application.sendNickChange(sender, message.getTargetText(), target.getPublicKey(), target.getNick());
            }


            Optional<String> previousNick = target.addOrUpdateContact(sender.asContact());
            previousNick.ifPresent(previous -> application.sendNickChange(target, previous, sender.getPublicKey(), sender.getNick()));

            String senderName = application.getIrcFormatter().getFullyQualifiedName(sender);
            target.getConnection().sendMessage(senderName, MessageType.PRIVMSG.getIrcCommand(), format(":%s", message.getText()));

        } else if(result.getContact().isPresent()) {
            Contact target = result.getContact().get();

            //previousNick.ifPresent(n -> message.getSender().getConnection().sendMessage(format(":%s NICK %s", n, target.get().getNick().get())));
            String targetNick = target.getNick().orElse(target.getPublicKey().getBase32SigningKey());

            if(!targetNick.equals(message.getTargetText())) {
                application.sendNickChange(sender, message.getTargetText(), target.getPublicKey(), targetNick);
            }

            application.getClientUserManager().sendNetworkMessage(sender, MessageType.PRIVMSG, target, format(":%s", message.getText()));
        } else {
            sender.getConnection().sendMessage(application.getServerName(), "401", format("%s :No such nick/channel", message.getTargetText()));
        }
    }

    public void processQuit(QuitMessage message, ClientUser sender) {

    }

    public void processUser(UserMessage message, ClientUser sender) {

    }

    public void processPing(PingMessage message, ClientUser sender) {
        String formatted = application.getIrcFormatter().formatMessage(application.getServerName(), MessageType.PONG.getIrcCommand(), format(":%s", message.getText()));
        sender.getConnection().sendMessage(formatted);
    }

    public void processPong(PongMessage message, ClientUser sender) {

    }

    public void processConnect(ConnectMessage message, ClientUser sender) {
        try {
            Socket socket = new Socket(message.getServer(), message.getPort().orElse(6667));
            ServerConnection connection = new ServerConnection(application, socket);
            new Thread(connection).start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
