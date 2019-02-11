package com.liph.chatterade.encryption;

import static java.lang.String.format;

import com.liph.chatterade.chat.models.ClientUser;
import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.encryption.SodiumLibrary;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.PublicKey;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;
import com.sun.jna.Platform;
import java.util.Arrays;
import java.util.Optional;


public class EncryptionService {

    private static final int SIGNATURE_SIZE = 64;
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

        // add salt so the hash set organization is unpredictable and can't be DoS'd
        hashCodeSalt = SodiumLibrary.randomBytes(16);
    }


    public KeyPair generateKeyPair() {
        try {
            return new KeyPair(SodiumLibrary.cryptoSignKeyPair());
        } catch(SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public ByteArray getMessageHash(byte[] message) {
        try {
            return new ByteArray(SodiumLibrary.cryptoGenerichash(message, 16, null));
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public int getHashCode(byte[] bytes) {
        try {
            byte[] hash = SodiumLibrary.cryptoGenerichash(bytes, 32, hashCodeSalt);
            return (hash[0] & 0xff) | ((hash[1] & 0xff) << 8) | ((hash[2] & 0xff) << 16) | ((hash[3] & 0xff) << 24);
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] encryptMessage(KeyPair sender, PublicKey target, ByteArray recentMessageHash, String message) {
        try {
            byte[] toSign = concat(recentMessageHash.getBytes(), getPublicKeyHash(target), message.getBytes());
            byte[] signature = SodiumLibrary.cryptoSignDetached(toSign, sender.getPrivateKey().getSigningKey().getBytes());

            byte[] salt = SodiumLibrary.randomBytes(4);
            byte[] saltedTargetPublicKey = getShortPublicKeyHash(salt, target);
            byte[] toEncrypt = concat(sender.getPublicKey().getSigningKey().getBytes(), signature, toSign);
            byte[] encryptedMessage = SodiumLibrary.cryptoBoxSeal(toEncrypt, target.getEncryptionKey());

            return concat(salt, saltedTargetPublicKey, encryptedMessage);

        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public Optional<DecryptedMessage> decryptMessage(KeyPair recipient, byte[] message) {
        if(message.length < 8 + Key.BYTE_SIZE + SIGNATURE_SIZE + 32) {
            System.out.println(format("Message was too short: %d < %d", message.length, 8 + Key.BYTE_SIZE + SIGNATURE_SIZE + 32));
            return Optional.empty();
        }

        byte[] salt = Arrays.copyOfRange(message, 0, 4);
        byte[] shortPublicKeyHash = Arrays.copyOfRange(message, 4, 8);

        if(!Arrays.equals(getShortPublicKeyHash(salt, recipient.getPublicKey()), shortPublicKeyHash)) {
            //System.out.println(format("%s was not the recipient.", recipient.getBase32SigningPublicKey()));
            return Optional.empty();
        }

        byte[] encryptedMessage = Arrays.copyOfRange(message, 8, message.length);
        byte[] decryptedMessage;

        try {
            decryptedMessage = SodiumLibrary.cryptoBoxSealOpen(encryptedMessage, recipient.getPublicKey().getEncryptionKey(), recipient.getPrivateKey().getEncryptionKey());
        } catch (SodiumLibraryException e) {
            System.out.println(format("Short hash matched, but %s was not the intended recipient.", recipient.getPublicKey().getBase32SigningKey()));
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

            if(!Arrays.equals(getPublicKeyHash(recipient.getPublicKey()), recipientPublicKeyHash)) {
                System.out.println(format("Signed message was not for recipient %s", recipient.getPublicKey().getBase32SigningKey()));
                return Optional.empty();
            }

            String messageStr = new String(signedMessage, 32, signedMessage.length - 32);

            return Optional.of(new DecryptedMessage(recentMessageHash, senderPublicKey, recipient.getPublicKey(), messageStr));

        } catch (SodiumLibraryException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    public byte[] getPublicKeyHash(PublicKey publicKey) {
        try {
            return SodiumLibrary.cryptoGenerichash(publicKey.getSigningKey().getBytes(), 16, null);
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] getShortPublicKeyHash(byte[] salt, PublicKey publicKey) {
        try {
            return Arrays.copyOf(SodiumLibrary.cryptoGenerichash(publicKey.getSigningKey().getBytes(), 16, salt), 4);
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
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
