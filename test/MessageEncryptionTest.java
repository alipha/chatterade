import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.encryption.models.Key;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.PublicKey;
import java.util.Optional;
import org.junit.jupiter.api.Test;


public class MessageEncryptionTest {

    @Test
    public void encryptionDecryptionTest() {
        EncryptionService encryptionService = EncryptionService.getInstance();

        ByteArray recentMessageHash = new ByteArray(new byte[] {123, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, -100});
        KeyPair sender = encryptionService.generateKeyPair();
        KeyPair recipient = encryptionService.generateKeyPair();
        PublicKey targetPublicKey = new PublicKey(recipient.getPublicKey().getSigningKey().getBytes());
        String message = "PRIVMSG Bob :testing";

        assertEquals(recipient.getPublicKey().getSigningKey(), targetPublicKey.getSigningKey());

        byte[] encryptedMessage = encryptionService.encryptMessage(sender, targetPublicKey, recentMessageHash, message);
        Optional<DecryptedMessage> decryptedMessage = encryptionService.decryptMessage(recipient, encryptedMessage);

        // expected path test
        assertTrue(decryptedMessage.isPresent());
        assertEquals(recentMessageHash, decryptedMessage.get().getRecentMessageHash());
        assertEquals(sender.getPublicKey().getSigningKey(), decryptedMessage.get().getSenderPublicKey());
        assertEquals(recipient.getPublicKey(), decryptedMessage.get().getTargetPublicKey());
        assertEquals(message, decryptedMessage.get().getMessage());

        // another user can't decrypt
        KeyPair nonRecipient = encryptionService.generateKeyPair();
        decryptedMessage = encryptionService.decryptMessage(nonRecipient, encryptedMessage);

        assertFalse(decryptedMessage.isPresent());

        // validate each part of the encryptedMessage can't be altered
        encryptedMessage[2] ^= 9;
        assertFalse(encryptionService.decryptMessage(recipient, encryptedMessage).isPresent());
        encryptedMessage[2] ^= 9;

        encryptedMessage[7] ^= 15;
        assertFalse(encryptionService.decryptMessage(recipient, encryptedMessage).isPresent());
        encryptedMessage[7] ^= 15;

        encryptedMessage[23] ^= 120;
        assertFalse(encryptionService.decryptMessage(recipient, encryptedMessage).isPresent());
        encryptedMessage[23] ^= 120;

        assertTrue(encryptionService.decryptMessage(recipient, encryptedMessage).isPresent());
    }


    @Test
    public void signedMessageDifferentRecipientTest() {

    }
}
