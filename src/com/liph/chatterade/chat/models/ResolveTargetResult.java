package com.liph.chatterade.chat.models;


import java.util.Optional;


public class ResolveTargetResult {

    private final Optional<Contact> contact;
    private final Optional<ClientUser> clientUser;


    public ResolveTargetResult(Optional<Contact> contact, Optional<ClientUser> clientUser) {
        this.contact = contact;
        this.clientUser = clientUser;
    }

    public ResolveTargetResult() {
        this.contact = Optional.empty();
        this.clientUser = Optional.empty();
    }


    public Optional<Contact> getContact() {
        return contact;
    }

    public Optional<ClientUser> getClientUser() {
        return clientUser;
    }
}
