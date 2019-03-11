package com.liph.chatterade.messaging;

import static java.lang.String.format;

import com.liph.chatterade.chat.Application;
import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.chat.models.ResolveTargetResult;
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
import java.util.Optional;


public class ServerMessageProcessor {

    private final Application application;


    public ServerMessageProcessor(Application application) {
        this.application = application;
    }


    public void processJoin(JoinMessage message, Contact sender, ClientUser recipient) {

    }

    public void processNick(NickMessage message, Contact sender, ClientUser recipient) {

    }

    public void processNotice(NoticeMessage message, Contact sender, ClientUser recipient) {

    }

    public void processPart(PartMessage message, Contact sender, ClientUser recipient) {

    }

    public void processPass(PassMessage message, Contact sender, ClientUser recipient) {

    }

    public void processPrivateMessage(PrivateMessage message, Contact sender, ClientUser recipient) {
        // TODO: separate out the ServerMessageProcessor code from the ClientMessageProcessor code

        ResolveTargetResult result = application.getClientUserManager().resolveTargetUser(message.getTarget(), Optional.empty());

        if(result.getClientUser().map(t -> !t.equals(recipient)).orElse(false))
            return;

        Optional<String> previousNick = application.getClientUserManager().addOrUpdateContact(recipient, sender);
        previousNick.ifPresent(previous -> application.sendNickChange(recipient, previous, sender.getPublicKey(), sender.getNick().get()));

        recipient.sendMessage(sender, MessageType.PRIVMSG.getIrcCommand(), format(":%s", message.getText()));
    }

    public void processQuit(QuitMessage message, Contact sender, ClientUser recipient) {

    }

    public void processUser(UserMessage message, Contact sender, ClientUser recipient) {

    }

    public void processPing(PingMessage message, Contact sender, ClientUser recipient) {

    }

    public void processPong(PongMessage message, Contact sender, ClientUser recipient) {

    }

    public void processConnect(ConnectMessage message, Contact sender, ClientUser recipient) {

    }
}
