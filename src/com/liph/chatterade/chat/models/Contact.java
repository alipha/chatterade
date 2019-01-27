package com.liph.chatterade.chat.models;


public class Contact extends User {

    private boolean sentNickMessage;


    public Contact(String nick) {
        super(nick);
    }


    public boolean hasSentNickMessage() {
        return sentNickMessage;
    }

    public void setSentNickMessage(boolean sentNickMessage) {
        this.sentNickMessage = sentNickMessage;
    }
}
