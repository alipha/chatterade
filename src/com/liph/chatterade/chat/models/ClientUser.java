package com.liph.chatterade.chat.models;

import static java.lang.String.format;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.connection.ClientConnection;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.PrivateKey;
import com.liph.chatterade.encryption.models.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ClientUser {

    private final ClientConnection connection;
    private final KeyPair keyPair;

    private String nick;
    private Optional<String> username = Optional.empty();
    private Optional<String> realName = Optional.empty();

    // TODO: keyed hash code to prevent DoS? Or limit the size of these Maps by removing unreplied-to contacts?
    private final Map<String, Contact> contactsByNick;
    private final Map<ByteArray, Contact> contactsByPublicKey;


    public ClientUser(String nick, KeyPair keyPair, ClientConnection connection) {
        this.nick = nick;
        this.connection = connection;
        this.keyPair = keyPair;
        this.contactsByNick = new HashMap<>();//new ConcurrentHashMap<>();
        this.contactsByPublicKey = new HashMap<>(); //new ConcurrentHashMap<>();
    }



    public ClientConnection getConnection() {
        return connection;
    }


    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public void setUsername(Optional<String> username) {
        this.username = username;
    }

    public Optional<String> getRealName() {
        return realName;
    }

    public void setRealName(Optional<String> realName) {
        this.realName = realName;
    }


    public PublicKey getPublicKey() {
        return keyPair.getPublicKey();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivateKey();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }


    public Optional<String> addOrUpdateContact(Contact contact) {
        return addOrUpdateContact(contact.getNick(), contact.getPublicKey());
    }

    // TODO: remove Optional<String> return?
    public synchronized Optional<String> addOrUpdateContact(Optional<String> nick, PublicKey publicKey) {
        Optional<Contact> contactByNick = nick.flatMap(this::getContactByNick);
        Optional<Contact> contactByKey = getContactByPublicKey(publicKey.getSigningKey());


        if(!nick.isPresent()) {
            if(!contactByKey.isPresent()) {
                addContact(new Contact(nick, publicKey));
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
                removeNick(contactByNick.get().getNick().get());    // TODO: override nick
                addContact(new Contact(nick, publicKey));
            } else {
                addContact(new Contact(nick, publicKey));
            }
        }

        return Optional.empty();
    }


    private synchronized void addContact(Contact user) {
        user.getNick().ifPresent(n -> contactsByNick.put(n.toLowerCase(), user));
        contactsByPublicKey.put(user.getPublicKey().getSigningKey(), user);
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
}
