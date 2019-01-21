package com.liph.chatterade.messaging.enums;

import com.liph.chatterade.messaging.models.JoinMessage;
import com.liph.chatterade.messaging.models.Message;
import com.liph.chatterade.messaging.models.NickMessage;
import com.liph.chatterade.messaging.models.NoticeMessage;
import com.liph.chatterade.messaging.models.PartMessage;
import com.liph.chatterade.messaging.models.PassMessage;
import com.liph.chatterade.messaging.models.PrivateMessage;
import com.liph.chatterade.messaging.models.QuitMessage;
import com.liph.chatterade.messaging.models.UserMessage;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.Optional;
import java.util.function.Function;


public enum MessageType {
    JOIN   ("JOIN"   , JoinMessage::new   ),
    NICK   ("NICK"   , NickMessage::new   ),
    NOTICE ("NOTICE" , NoticeMessage::new ),
    PART   ("PART"   , PartMessage::new   ),
    PASS   ("PASS"   , PassMessage::new   ),
    PRIVMSG("PRIVMSG", PrivateMessage::new),
    QUIT   ("QUIT"   , QuitMessage::new   ),
    USER   ("USER"   , UserMessage::new   );


    private final String ircCommand;
    private final Function<TokenizedMessage, Message> constructor;


    MessageType(String ircCommand, Function<TokenizedMessage, Message> constructor) {
        this.ircCommand = ircCommand;
        this.constructor = constructor;
    }


    public String getIrcCommand() {
        return ircCommand;
    }

    public Function<TokenizedMessage, Message> getConstructor() {
        return constructor;
    }


    public static Optional<MessageType> fromIrcCommand(String ircCommand) {

        for(MessageType messageType : values())
            if(messageType.ircCommand.equalsIgnoreCase(ircCommand))
                return Optional.of(messageType);

        return Optional.empty();
    }
}
