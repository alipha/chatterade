package com.liph.chatterade.chat.models;

import static java.lang.String.format;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.encryption.models.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class ClientUser extends User {

    private final ClientConnection connection;

    // TODO: keyed hash code to prevent DoS? Or limit the size of these Maps by removing unreplied-to contacts?
    private final Map<String, Contact> contactsByNick;
    private final Map<ByteArray, Contact> contactsByPublicKey;


    public ClientUser(String nick, ClientConnection connection) {
        super(nick);
        this.connection = connection;
        this.contactsByNick = new HashMap<>();//new ConcurrentHashMap<>();
        this.contactsByPublicKey = new HashMap<>(); //new ConcurrentHashMap<>();
    }


    public Optional<String> addOrUpdateContact(User contact) {
        return addOrUpdateContact(contact.getNick(), contact.getKey());
    }

    // TODO: remove Optional<String> return?
    public synchronized Optional<String> addOrUpdateContact(Optional<String> nick, Optional<Key> key) {
        Optional<Contact> contactByNick = nick.flatMap(this::getContactByNick);
        Optional<Contact> contactByKey = key.map(Key::getSigningPublicKey).flatMap(this::getContactByPublicKey);

        if(!nick.isPresent() && !key.isPresent())   // TODO: error?
            return Optional.empty();

        if(nick.isPresent() && !key.isPresent()) {
            if(!contactByNick.isPresent()) {
                addContact(new Contact(nick, key));
            }
            return Optional.empty();
        }

        if(!nick.isPresent() && key.isPresent()) {
            if(!contactByKey.isPresent()) {
                addContact(new Contact(nick, key));
            }
            return Optional.empty();
        }

        if(contactByKey.isPresent()) {
            if(contactByNick.isPresent()) {
                if(contactByNick.equals(contactByKey)) {
                    return Optional.empty();
                } else {
                    removeNick(nick.get());    // TODO: override nick
                    return renameNick(contactByKey, nick);    // TODO: override nick
                }
            } else {
                return renameNick(contactByKey, nick);
            }
        } else {
            if(contactByNick.isPresent()) {
                if(contactByNick.get().getKey().isPresent()) {
                    removeNick(contactByNick.get().getNick().get());    // TODO: override nick
                    addContact(new Contact(nick, key));
                } else {
                    contactByNick.get().setKey(key);
                    addContact(contactByNick.get());
                }
            } else {
                addContact(new Contact(nick, key));
            }
        }

        return Optional.empty();
    }


    private synchronized void addContact(Contact user) {
        user.getNick().ifPresent(n -> contactsByNick.put(n.toLowerCase(), user));
        user.getKey().ifPresent(k -> contactsByPublicKey.put(k.getSigningPublicKey(), user));
    }

    private synchronized Optional<String> renameNick(Optional<Contact> contact, Optional<String> nick) {
        if(!contact.isPresent() || !nick.isPresent()) {
            throw new IllegalStateException(format("renameNick expects both contact and nick to be present. contact.isPresent=%b, nick.isPresent=%b",
                    contact.isPresent(), nick.isPresent()));
        }

        Optional<String> previousNick = contact.get().getNick();
        previousNick.ifPresent(this::removeNick);
        contact.get().setNick(nick);

        addContact(contact.get());
        return previousNick;
    }

    private synchronized void removeNick(String nick) {
        contactsByNick.remove(nick.toLowerCase());
    }

    /*
    public Pair<Optional<Contact>, Boolean> getAndUpdateContactByTarget(Target target) {
        Optional<Contact> contact = target.getPublicKey().flatMap(this::getContactByPublicKey);
        boolean updateNick = false;

        if(!target.getNick().isPresent()) {
            return Pair.of(contact, false);
        }

        if(!contact.isPresent()) {
            contact = target.getNick().flatMap(this::getContactByNick);
            return Pair.of(contact, false);
        }
                if(contact.get().getNick().isPresent()) {
                    updateNick = target.getNick().get().equals(contact.get().getNick().get());

            } else {

            }


        return Pair.of(contact, updateNick);
    }
    */

    public synchronized Optional<Contact> getContactByNick(String nick) {
        return Optional.ofNullable(contactsByNick.get(nick.toLowerCase()));
    }

    public synchronized Optional<Contact> getContactByPublicKey(ByteArray key) {
        return Optional.ofNullable(contactsByPublicKey.get(key));
    }


    public ClientConnection getConnection() {
        return connection;
    }
}
