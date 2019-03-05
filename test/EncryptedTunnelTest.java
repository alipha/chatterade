import static org.junit.jupiter.api.Assertions.assertTrue;

import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.models.Nonce;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class EncryptedTunnelTest {

    @Test
    public void nonceTest() {
        EncryptionService.getInstance();

        byte[] expected = new byte[EncryptionService.NONCE_SIZE];

        Nonce nonce = new Nonce();
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        nonce.increment();
        expected[expected.length - 1] = 1;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        for(int i = 1; i < 127; i++)
            nonce.increment();
        expected[expected.length - 1] = 127;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        nonce.increment();
        expected[expected.length - 1] = -128;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        nonce.increment();
        expected[expected.length - 1] = -127;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        for(int i = -127; i < -1; i++)
            nonce.increment();
        expected[expected.length - 1] = -1;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        nonce.increment();
        expected[expected.length - 2] = 1;
        expected[expected.length - 1] = 0;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        for(int i = 0; i < 255; i++)
            nonce.increment();
        expected[expected.length - 1] = -1;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        nonce.increment();
        expected[expected.length - 2] = 2;
        expected[expected.length - 1] = 0;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        for(int i = 0; i < 254*256-1; i++)
            nonce.increment();
        expected[expected.length - 2] = -1;
        expected[expected.length - 1] = -1;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));

        nonce.increment();
        expected[expected.length - 3] = 1;
        expected[expected.length - 2] = 0;
        expected[expected.length - 1] = 0;
        assertTrue(Arrays.equals(expected, nonce.getBytes()));
    }
}
