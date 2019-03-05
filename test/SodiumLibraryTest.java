import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.SodiumLibrary;
import com.liph.chatterade.encryption.models.SodiumKxKeyPair;
import com.muquit.libsodiumjna.SodiumKeyPair;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class SodiumLibraryTest {

    @Test
    public void keyExchangeTest() {
        EncryptionService.getInstance();

        SodiumKeyPair clientKeyPair = SodiumLibrary.cryptoKxKeyPair();
        SodiumKeyPair serverKeyPair = SodiumLibrary.cryptoKxKeyPair();

        SodiumKxKeyPair clientSymmetricKeys = SodiumLibrary.cryptoKxClientSessionKeys(clientKeyPair.getPublicKey(), clientKeyPair.getPrivateKey(), serverKeyPair.getPublicKey());
        SodiumKxKeyPair serverSymmetricKeys = SodiumLibrary.cryptoKxServerSessionKeys(serverKeyPair.getPublicKey(), serverKeyPair.getPrivateKey(), clientKeyPair.getPublicKey());

        assertFalse(Arrays.equals(clientKeyPair.getPrivateKey(), serverKeyPair.getPrivateKey()));
        assertTrue(Arrays.equals(clientSymmetricKeys.getReceivingKey(), serverSymmetricKeys.getTransmittingKey()));
        assertTrue(Arrays.equals(serverSymmetricKeys.getReceivingKey(), clientSymmetricKeys.getTransmittingKey()));
    }
}
