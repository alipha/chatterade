package com.liph.chatterade.parsing;

import static java.lang.String.format;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.encryption.models.PublicKey;


public class IrcFormatter {

    public String formatMessage(String sender, String messageType, String message) {
        return format(":%s %s %s", sender, messageType, message);
    }

    public String formatMessage(String sender, String messageType, String nick, String message) {
        return format(":%s %s %s %s", sender, messageType, nick, message);
    }

    public String replaceSender(String message, ClientUser sender) {
        String senderName = getFullyQualifiedName(sender);

        if(message.startsWith(":")) {
            int spaceIndex = message.indexOf(' ');
            if(spaceIndex >= 0)
                message = message.substring(spaceIndex + 1);
            else
                message = "";
        }

        return format(":%s %s", senderName, message);
    }

    public String getFullyQualifiedName(ClientUser user) {
        return getFullyQualifiedName(user.getNick(), user.getUsername().orElse("unknown"), user.getPublicKey());
    }

    public String getFullyQualifiedName(Contact contact) {
        String publicKey = contact.getPublicKey().getBase32SigningKey();
        return getFullyQualifiedName(contact.getNick().orElse(publicKey), "unknown", contact.getPublicKey());
    }

    public String getFullyQualifiedName(String nick, String username, PublicKey publicKey) {
        return format("%s!%s@%s", nick, username, publicKey.getBase32SigningKey());
    }
}
