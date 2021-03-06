package com.liph.chatterade.encryption;

import static java.lang.String.format;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.Nonce;
import com.liph.chatterade.encryption.models.PublicKey;
import com.liph.chatterade.encryption.exceptions.SodiumLibraryException;
import com.liph.chatterade.encryption.models.SodiumKxKeyPair;
import com.muquit.libsodiumjna.SodiumKeyPair;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;


/*
TODO: get rid of magic numbers
 */
public class EncryptionService {

    public static final int SIGNATURE_SIZE = 64;
    public static int NONCE_SIZE;

    private static boolean isInitialized = false;
    
    private static EncryptionService instance;
    

    private final byte[] hashCodeSalt;

    
    public static EncryptionService getInstance() {
        if(instance == null)
            instance = new EncryptionService();
        return instance;
    }
    

    private EncryptionService() {
        if(!isInitialized) {
            isInitialized = true;
            initialize();
        }

        NONCE_SIZE = SodiumLibrary.cryptoSecretBoxNonceBytes().intValue();

        // add salt so the hash set organization is unpredictable and can't be DoS'd
        hashCodeSalt = SodiumLibrary.randomBytes(16);
    }


    public byte[] randomNonce() {
        return SodiumLibrary.randomBytes(NONCE_SIZE);
    }

    public KeyPair generateKeyPair() {
        return new KeyPair(SodiumLibrary.cryptoSignKeyPair());
    }

    public SodiumKeyPair generateKeyExchangeKeyPair() {
        return SodiumLibrary.cryptoKxKeyPair();
    }


    public ByteArray deriveKey(String nick, String password) {
        byte[] salt = SodiumLibrary.cryptoGenerichash(nick.toLowerCase().getBytes(), 16, "chatterade".getBytes());

        // lock to prevent DoS by out of memory errors
        // TODO: rate-limit derivedKey to prevent DoS
        synchronized (this) {
            return new ByteArray(SodiumLibrary.cryptoPwhash(password.getBytes(), salt, 3, new NativeLong(1 << 23), 2));
            //return SodiumLibrary.deriveKey(password.getBytes(), salt);
        }
    }


    public ByteArray getMessageHash(byte[] message) {
        return new ByteArray(SodiumLibrary.cryptoGenerichash(message, 16, null));
    }

    public String getFilenameHash(byte[] message) {
        return getMessageHash(message).toString();
    }


    public int getHashCode(byte[] bytes) {
        byte[] hash = SodiumLibrary.cryptoGenerichash(bytes, 16, hashCodeSalt);
        return (hash[0] & 0xff) | ((hash[1] & 0xff) << 8) | ((hash[2] & 0xff) << 16) | ((hash[3] & 0xff) << 24);
    }


    public byte[] encryptMessage(byte[] message, Nonce nonce, byte[] symmetricKey) {
        return SodiumLibrary.cryptoSecretBoxEasy(message, nonce.getBytes(), symmetricKey);
    }

    public byte[] decryptMessage(byte[] message, Nonce nonce, byte[] symmetricKey) {
        return SodiumLibrary.cryptoSecretBoxOpenEasy(message, nonce.getBytes(), symmetricKey);
    }


    public byte[] encryptMessage(KeyPair sender, PublicKey target, ByteArray recentMessageHash, String message) {
        byte[] toSign = concat(recentMessageHash.getBytes(), getPublicKeyHash(target), message.getBytes());
        byte[] signature = SodiumLibrary.cryptoSignDetached(toSign, sender.getPrivateKey().getSigningKey().getBytes());

        byte[] salt = SodiumLibrary.randomBytes(4);
        byte[] saltedTargetPublicKey = getShortPublicKeyHash(salt, target);
        byte[] toEncrypt = concat(sender.getPublicKey().getSigningKey().getBytes(), signature, toSign);
        byte[] encryptedMessage = SodiumLibrary.cryptoBoxSeal(toEncrypt, target.getEncryptionKey());

        return concat(salt, saltedTargetPublicKey, encryptedMessage);
    }


    public Optional<DecryptedMessage> decryptMessage(ClientUser recipientUser, byte[] message) {
        KeyPair recipientKey = recipientUser.getKeyPair();

        if(message.length < 8 + Key.BYTE_SIZE + SIGNATURE_SIZE + 32) {
            System.out.println(format("Message was too short: %d < %d", message.length, 8 + Key.BYTE_SIZE + SIGNATURE_SIZE + 32));
            return Optional.empty();
        }

        byte[] salt = Arrays.copyOfRange(message, 0, 4);
        byte[] shortPublicKeyHash = Arrays.copyOfRange(message, 4, 8);

        if(!Arrays.equals(getShortPublicKeyHash(salt, recipientKey.getPublicKey()), shortPublicKeyHash)) {
            //System.out.println(format("%s was not the recipient.", recipientKey.getBase32SigningPublicKey()));
            return Optional.empty();
        }

        byte[] encryptedMessage = Arrays.copyOfRange(message, 8, message.length);
        byte[] decryptedMessage;

        try {
            decryptedMessage = SodiumLibrary.cryptoBoxSealOpen(encryptedMessage, recipientKey.getPublicKey().getEncryptionKey(), recipientKey.getPrivateKey().getEncryptionKey());
        } catch (SodiumLibraryException e) {
            System.out.println(format("Short hash matched, but %s was not the intended recipient.", recipientKey.getPublicKey().getBase32SigningKey()));
            return Optional.empty();
        }

        if(decryptedMessage.length < Key.BYTE_SIZE + SIGNATURE_SIZE + 32) {
            System.out.println(format("Decrypted message was too short: %d < %d", decryptedMessage.length, Key.BYTE_SIZE + SIGNATURE_SIZE + 32));
            return Optional.empty();
        }

        try {
            ByteArray senderPublicKey = new ByteArray(decryptedMessage, Key.BYTE_SIZE);

            int signatureEndPos = Key.BYTE_SIZE + SIGNATURE_SIZE;
            byte[] signature = Arrays.copyOfRange(decryptedMessage, Key.BYTE_SIZE, signatureEndPos);
            byte[] signedMessage = Arrays.copyOfRange(decryptedMessage, signatureEndPos, decryptedMessage.length);

            if(!SodiumLibrary.cryptoSignVerifyDetached(signature, signedMessage, senderPublicKey.getBytes())) {
                System.out.println("Signature did not match.");
                return Optional.empty();
            }

            ByteArray recentMessageHash = new ByteArray(signedMessage, 16);
            byte[] recipientPublicKeyHash = Arrays.copyOfRange(signedMessage, 16, 32);

            if(!Arrays.equals(getPublicKeyHash(recipientKey.getPublicKey()), recipientPublicKeyHash)) {
                System.out.println(format("Signed message was not for recipient %s", recipientKey.getPublicKey().getBase32SigningKey()));
                return Optional.empty();
            }

            String messageStr = new String(signedMessage, 32, signedMessage.length - 32);

            return Optional.of(new DecryptedMessage(recentMessageHash, senderPublicKey, recipientUser, messageStr));

        } catch (SodiumLibraryException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    public void saveEncryptedToFile(String filename, byte[] contents, byte[] key) throws IOException {
        Nonce nonce = Nonce.random();
        byte[] encrypted = encryptMessage(contents, nonce, key);

        FileOutputStream file = new FileOutputStream(filename);
        file.write(nonce.getBytes());
        file.write(encrypted);
        file.close();

        file = new FileOutputStream(filename + ".txt");     // TODO: REMOVE!!
        file.write(contents);
        file.close();
    }


    public byte[] decryptFile(String filename, byte[] key) throws IOException {
        byte[] fileBytes = Files.readAllBytes(new File(filename).toPath());
        Nonce nonce = new Nonce(Arrays.copyOf(fileBytes, EncryptionService.NONCE_SIZE));
        byte[] encryptedBytes = Arrays.copyOfRange(fileBytes, EncryptionService.NONCE_SIZE, fileBytes.length);

        return decryptMessage(encryptedBytes, nonce, key);
    }


    public SodiumKxKeyPair performKeyExchange(boolean isClient, Consumer<byte[]> sendKey, Function<Integer, byte[]> receiveKey) {
        SodiumKeyPair keyExchangeKeys = EncryptionService.getInstance().generateKeyExchangeKeyPair();
        sendKey.accept(keyExchangeKeys.getPublicKey());
        byte[] remotePublicKey = receiveKey.apply(SodiumLibrary.crytoKxPublicKeyBytes().intValue());

        if(isClient) {
            return SodiumLibrary.cryptoKxClientSessionKeys(keyExchangeKeys.getPublicKey(), keyExchangeKeys.getPrivateKey(), remotePublicKey);
        } else {
            return SodiumLibrary.cryptoKxServerSessionKeys(keyExchangeKeys.getPublicKey(), keyExchangeKeys.getPrivateKey(), remotePublicKey);
        }
    }


    public byte[] getPublicKeyHash(PublicKey publicKey) {
        return SodiumLibrary.cryptoGenerichash(publicKey.getSigningKey().getBytes(), 16, null);
    }


    public byte[] getShortPublicKeyHash(byte[] salt, PublicKey publicKey) {
        return Arrays.copyOf(SodiumLibrary.cryptoGenerichash(publicKey.getSigningKey().getBytes(), 16, salt), 4);
    }


    public byte[] getEncryptionPublicKey(ByteArray signingKey) {
        return SodiumLibrary.cryptoSignEdPkTOcurvePk(signingKey.getBytes());
    }


    public byte[] getEncryptionPrivateKey(ByteArray signingKey) {
        return SodiumLibrary.cryptoSignEdSkTOcurveSk(signingKey.getBytes());
    }


    private void initialize() {
        String libraryPath;

        if (Platform.isMac())
        {
            // MacOS
            libraryPath = "/usr/local/lib/libsodium.dylib";
        }
        else if (Platform.isWindows())
        {
            // Windows
            libraryPath = "./libsodium.dll";
        }
        else
        {
            // Linux
            libraryPath = "/usr/local/lib/libsodium.so";
        }

        SodiumLibrary.setLibraryPath(libraryPath);

        String v = SodiumLibrary.libsodiumVersionString();
        System.out.println("libsodium version: " + v);
    }


    private byte[] concat(byte[]... arrays) {
        int totalLen = 0;
        for(byte[] array : arrays)
            totalLen += array.length;

        byte[] result = new byte[totalLen];
        int destPos = 0;

        for(byte[] array : arrays) {
            System.arraycopy(array, 0, result, destPos, array.length);
            destPos += array.length;
        }

        return result;
    }
}
