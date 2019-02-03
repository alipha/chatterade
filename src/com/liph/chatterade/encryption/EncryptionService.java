package com.liph.chatterade.encryption;

import static java.lang.String.format;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.encryption.models.Key;
import com.muquit.libsodiumjna.SodiumLibrary;
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
    

    public EncryptionService() {
        if(!isInitialized) {
            isInitialized = true;
            initialize();
        }

        // add salt so the hash set organization is unpredictable and can't be DoS'd
        hashCodeSalt = SodiumLibrary.randomBytes(16);
    }


    public Key generateKey() {
        try {
            return new Key(SodiumLibrary.cryptoSignKeyPair());
        } catch(SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public ByteArray getMessageHash(String message) {
        try {
            return new ByteArray(SodiumLibrary.cryptoGenerichash(message.getBytes(), 16));
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public int getHashCode(byte[] bytes) {
        try {
            byte[] hash = SodiumLibrary.cryptoGenerichash(concat(hashCodeSalt, bytes), 16);
            return (hash[0] & 0xff) | ((hash[1] & 0xff) << 8) | ((hash[2] & 0xff) << 16) | ((hash[3] & 0xff) << 24);
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] encryptMessage(Key sender, Key target, ByteArray recentMessageHash, String message) {
        try {
            byte[] toSign = concat(getPublicKeyHash(target), message.getBytes());
            byte[] signature = SodiumLibrary.cryptoSignDetached(toSign, sender.getSigningPrivateKey().get());

            byte[] salt = SodiumLibrary.randomBytes(4);
            byte[] saltedTargetPublicKey = getShortPublicKeyHash(salt, target);
            byte[] toEncrypt = concat(sender.getSigningPublicKey().getBytes(), signature, toSign);
            byte[] encryptedMessage = SodiumLibrary.cryptoBoxSeal(toEncrypt, target.getDHPublicKey());

            return concat(recentMessageHash.getBytes(), salt, saltedTargetPublicKey, encryptedMessage);

        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public Optional<DecryptedMessage> decryptMessage(Key recipient, byte[] message) {
        ByteArray recentMessageHash = new ByteArray(message, 16);   // TODO: check recentMessage prior to calling this method

        byte[] salt = Arrays.copyOfRange(message, 16, 20);
        byte[] shortPublicKeyHash = Arrays.copyOfRange(message, 20, 24);

        if(!Arrays.equals(getShortPublicKeyHash(salt, recipient), shortPublicKeyHash)) {
            System.out.println(format("%s was not the recipient.", recipient.getBase32SigningPublicKey()));
            return Optional.empty();
        }

        byte[] encryptedMessage = Arrays.copyOfRange(message, 24, message.length);
        byte[] decryptedMessage;

        try {
            decryptedMessage = SodiumLibrary.cryptoBoxSealOpen(encryptedMessage, recipient.getDHPublicKey(), recipient.getDHPrivateKey().get());
        } catch (SodiumLibraryException e) {
            System.out.println(format("Short hash matched, but %s was not the intended recipient.", recipient.getBase32SigningPublicKey()));
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

            byte[] recipientPublicKeyHash = Arrays.copyOf(signedMessage, 16);

            if(!Arrays.equals(getPublicKeyHash(recipient), recipientPublicKeyHash)) {
                System.out.println(format("Signed message was not for recipient %s", recipient.getBase32SigningPublicKey()));
                return Optional.empty();
            }

            String messageStr = new String(signedMessage, 16, signedMessage.length - 16);

            return Optional.of(new DecryptedMessage(recentMessageHash, senderPublicKey, recipient, messageStr));

        } catch (SodiumLibraryException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    public byte[] getPublicKeyHash(Key key) {
        try {
            return SodiumLibrary.cryptoGenerichash(key.getSigningPublicKey().getBytes(), 16);
        } catch (SodiumLibraryException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] getShortPublicKeyHash(byte[] salt, Key key) {
        try {
            return Arrays.copyOf(SodiumLibrary.cryptoGenerichash(concat(salt, key.getSigningPublicKey().getBytes()), 16), 4);
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
