import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.DecryptedMessage;
import com.liph.chatterade.encryption.models.Key;
import java.util.Optional;
import org.junit.jupiter.api.Test;


public class MessageEncryptionTest {

    @Test
    public void encryptionDecryptionTest() {
        EncryptionService encryptionService = new EncryptionService();

        ByteArray recentMessageHash = new ByteArray(new byte[] {123, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, -100});
        Key sender = encryptionService.generateKey();
        Key recipient = encryptionService.generateKey();
        Key targetNoPrivateKey = new Key(recipient.getSigningPublicKey().getBytes());
        String message = "PRIVMSG Bob :testing";

        assertEquals(recipient.getSigningPublicKey(), targetNoPrivateKey.getSigningPublicKey());

        byte[] encryptedMessage = encryptionService.encryptMessage(sender, targetNoPrivateKey, recentMessageHash, message);
        Optional<DecryptedMessage> decryptedMessage = encryptionService.decryptMessage(recipient, encryptedMessage);

        // expected path test
        assertTrue(decryptedMessage.isPresent());
        assertEquals(recentMessageHash, decryptedMessage.get().getRecentMessageHash());
        assertEquals(sender.getSigningPublicKey(), decryptedMessage.get().getSenderPublicKey());
        assertEquals(recipient, decryptedMessage.get().getTargetPublicKey());
        assertEquals(message, decryptedMessage.get().getMessage());

        // another user can't decrypt
        Key nonRecipient = encryptionService.generateKey();
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
