package com.liph.chatterade.messaging;

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


public interface MessageProcessor {

    void processJoin(JoinMessage message);

    void processNick(NickMessage message);

    void processNotice(NoticeMessage message);

    void processPart(PartMessage message);

    void processPass(PassMessage message);

    void processPrivateMessage(PrivateMessage message);

    void processQuit(QuitMessage message);

    void processUser(UserMessage message);

    void processPing(PingMessage message);

    void processPong(PongMessage message);

    void processConnect(ConnectMessage message);
}
