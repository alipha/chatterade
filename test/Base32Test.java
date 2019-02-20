import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.liph.chatterade.common.Base32Encoder;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class Base32Test {

    @Test
    public void encodeDecodeTest() {
        byte[] testBytes = new byte[] {-40, -126, 63, 105, -77, -68, -98, -8, 108, 50, -127, -128, 127, 0, 0, 34 };

        for(int i = 0; i <= testBytes.length; i++) {
            byte[] expected = Arrays.copyOf(testBytes, i);

            String base32 = Base32Encoder.getBase32(expected);
            byte[] actual = Base32Encoder.getBytes(base32);
            byte[] actualUpperCase = Base32Encoder.getBytes(base32.toUpperCase());

            System.out.println(base32);
            assertValidChars(base32);
            assertTrue(Arrays.equals(expected, actual));
            assertTrue(Arrays.equals(expected, actualUpperCase));
        }
    }


    @Test()
    public void invalidCharsTest() {
        for(int i = 0; i < 0xffff; i++) {
            boolean valid = "0123456789abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUV".indexOf(i) >= 0;

            String base32 = "om0vj" + (char)i + "fm96";

            try {
                Base32Encoder.getBytes(base32);
                if(!valid)
                    fail(base32 + " is an invalid base32 string with char " + i);
            } catch(IllegalArgumentException e) {
                if(valid)
                    fail(base32 + " is a valid base32 string with char " + i);
            }
        }
    }


    @Test()
    public void invalidLengthTest() {
        try {
            Base32Encoder.getBytes("om0vjafm961");
            fail("Exception should be thrown for invalid length");
        } catch(IllegalArgumentException e) {
            assertEquals("Base32 string ends with partial value: 4", e.getMessage());
        }
    }


    private void assertValidChars(String base32) {
        for(int i = 0; i < base32.length(); i++)
            assertTrue("0123456789abcdefghijklmnopqrstuv".indexOf(base32.charAt(i)) >= 0);
    }
}
