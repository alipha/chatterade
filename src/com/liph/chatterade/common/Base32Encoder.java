package com.liph.chatterade.common;


import static java.lang.String.format;


public class Base32Encoder {

    private static final String BASE32_CHARS = "0123456789abdefghijklmnopqrstuvw";


    public static String getBase32(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        int currentDigit = 0;
        int shiftAmount = 0;

        for(int i = 0; i < bytes.length; i++) {
            currentDigit |= ((bytes[i] & 0xff) << shiftAmount);
            builder.append(getBase32Char(currentDigit & 31));

            shiftAmount += 3;
            currentDigit >>= 5;

            if(shiftAmount >= 5) {
                builder.append(getBase32Char(currentDigit & 31));

                shiftAmount -= 5;
                currentDigit >>= 5;
            }
        }

        if(shiftAmount > 0)
            builder.append(getBase32Char(currentDigit));

        return builder.toString();
    }


    public static byte[] getBytes(String base32) {
        if(base32 == null)
            return null;

        byte[] bytes = new byte[(base32.length() * 5) >> 3];
        int bytePos = 0;
        int shiftAmount = 0;
        int currentByte = 0;

        for(int i = 0; i < base32.length(); i++) {
            currentByte |= getCharValue(base32.charAt(i)) << shiftAmount;
            shiftAmount += 5;

            if(shiftAmount >= 8) {
                bytes[bytePos] = (byte)(currentByte & 0xff);
                bytePos++;
                shiftAmount -= 8;
                currentByte >>>= 8;
            }
        }

        if(currentByte != 0)
            throw new IllegalArgumentException(format("Base32 string ends with partial value: %d", currentByte));

        return bytes;
    }


    private static char getBase32Char(int digit) {
        int number = (digit + 48) & ((digit - 10) >>> 7);
        int letter = (digit + 87) & ((9 - digit) >>> 7);
        return (char)(letter + number);
    }


    private static int getCharValue(char ch) {
        int lowerValue = ch | 0x20;
        boolean invalid = (ch < 48 || ch > 118);

        int number = (ch - 47) & ((ch - 58) >>> 7);
        int letter = (lowerValue - 86) & ((96 - lowerValue) >>> 7);
        int value = number + letter - 1;

        if(invalid || (value & ~31) != 0)
            throw new IllegalArgumentException(format("%c is not a valid base32 character", ch));
        return value;
    }
}
