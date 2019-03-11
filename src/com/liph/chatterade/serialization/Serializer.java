package com.liph.chatterade.serialization;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.chat.models.Contact;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.common.LockManager;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.PrivateKey;
import com.liph.chatterade.encryption.models.PublicKey;
import org.ini4j.Ini;

import java.io.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.String.format;


public class Serializer {

    private final LockManager lockManager;
    private final Set<ByteArray> passwordKeys;


    public Serializer(LockManager lockManager) {
        this.lockManager = lockManager;
        this.passwordKeys = ConcurrentHashMap.newKeySet();
    }


    public void delayedSave(ClientUser user) {
        if(!user.getPasswordKey().isPresent())
            return;

        ByteArray passwordKey = user.getPasswordKey().get();
        if(!passwordKeys.add(passwordKey))
            return;

        new Thread(() -> {
            try {
                Thread.sleep(120 * 1000);
                passwordKeys.remove(passwordKey);
                save(user);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void save(ClientUser user) {
        if(!user.getPasswordKey().isPresent())
            return;

        ByteArray passwordKey = user.getPasswordKey().get();
        try {
            synchronized (lockManager.get(passwordKey)) {
                doSave(user);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            lockManager.release(passwordKey);
        }
    }

    private void doSave(ClientUser user) throws IOException {
        if(!user.getPasswordKey().isPresent())
            return;

        byte[] key = user.getPasswordKey().get().getBytes();
        String filenameHash = EncryptionService.getInstance().getFilenameHash(key);

        Ini ini = new Ini();
        ini.put("user", "public-key", user.getPublicKey().getBase32SigningKey());
        ini.put("user", "private-key", user.getPrivateKey().getBase32SigningKey());

        int i = 0;
        for(Contact contact : user.getContacts()) {
            saveContact(ini, ++i, contact);
        }

        StringWriter output = new StringWriter();
        ini.store(output);
        EncryptionService.getInstance().saveEncryptedToFile(getFilename(filenameHash), output.toString().getBytes(), key);
    }


    public ClientUser load(Optional<ByteArray> passwordKey, Function<KeyPair, ClientUser> createUser) {
        Optional<ClientUser> user = Optional.empty();
        try {
            user = doLoad(passwordKey, createUser);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return user.orElseGet(() -> createUser.apply(EncryptionService.getInstance().generateKeyPair()));
    }

    private Optional<ClientUser> doLoad(Optional<ByteArray> passwordKey, Function<KeyPair, ClientUser> createUser) throws IOException {
        if(!passwordKey.isPresent())
            return Optional.empty();

        byte[] key = passwordKey.get().getBytes();
        String filenameHash = EncryptionService.getInstance().getFilenameHash(key);

        byte[] decrypted = EncryptionService.getInstance().decryptFile(getFilename(filenameHash), key);

        Ini ini = new Ini(new ByteArrayInputStream(decrypted));
        String base32publicKey = ini.get("user", "public-key");
        String base32privateKey = ini.get("user", "private-key");
        KeyPair keyPair = new KeyPair(new PublicKey(base32publicKey), new PrivateKey(base32privateKey));

        ClientUser user = createUser.apply(keyPair);

        Ini.Section section;
        int contactIndex = 0;

        while((section = ini.get(format("contact%d", ++contactIndex))) != null) {
            loadContact(section, user);
        }

        return Optional.of(user);
    }


    private void saveContact(Ini ini, int index, Contact contact) {
        String section = format("contact%d", index);
        contact.getNick().ifPresent(n -> ini.put(section, "nick", n));
        contact.getUsername().ifPresent(u -> ini.put(section, "username", u));
        ini.put(section, "public-key", contact.getPublicKey().getBase32SigningKey());
    }

    private void loadContact(Ini.Section section, ClientUser user) {
        Optional<String> nick = Optional.ofNullable(section.get("nick"));
        Optional<String> username = Optional.ofNullable(section.get("username"));
        PublicKey publicKey = new PublicKey(section.get("public-key"));
        user.addOrUpdateContact(new Contact(nick, username, publicKey));
    }


    private String getFilename(String filenameHash) {
        String userHomeDir = System.getProperty("user.home");
        return format("%s/.chatterade/u%s", userHomeDir, filenameHash);
    }
}
