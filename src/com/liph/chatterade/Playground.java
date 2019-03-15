package com.liph.chatterade;

import static java.lang.String.format;

import com.liph.chatterade.encryption.EncryptionService;
import com.liph.chatterade.encryption.SodiumLibrary;
import com.liph.chatterade.encryption.exceptions.SodiumLibraryException;
import com.liph.chatterade.encryption.models.KeyPair;
import com.liph.chatterade.encryption.models.Nonce;
import com.muquit.libsodiumjna.SodiumKeyPair;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class Playground {


    public static void passwordTiming(String[] args) {
        EncryptionService encryptionService = EncryptionService.getInstance();
        long start = System.currentTimeMillis();

        for(int i = 0; i < 1000; i++) {
            encryptionService.deriveKey("Kevin", "Password");
        }

        long end = System.currentTimeMillis();
        System.out.println(format("%.2f ms", (end - start) / 100.0));
    }


    public static void hashTiming() {
        int iterations = 1000000;

        EncryptionService.getInstance();
        byte[] input = SodiumLibrary.randomBytes(32);
        byte[] key = SodiumLibrary.cryptoShortHashKeygen();
        byte[] hash = new byte[16];

        long start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++)
            SodiumLibrary.cryptoShortHash(input, key);
        long end = System.currentTimeMillis();

        long countPerSec = iterations*1000L / (end - start);
        System.out.println(format("cryptoShortHash: %d/sec", countPerSec));


        Nonce n = new Nonce();
        start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++)
            n.increment();
        end = System.currentTimeMillis();

        countPerSec = iterations*1000L / (end - start);
        System.out.println(format("      increment: %d/sec", countPerSec));


        input = n.getBytes();
        start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++)
            SodiumLibrary.increment(input);
        end = System.currentTimeMillis();

        countPerSec = iterations*1000L / (end - start);
        System.out.println(format("sodium_increment: %d/sec", countPerSec));

/*
        start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++)
            SodiumLibrary.cryptoGenerichash(input, 16, key);
        end = System.currentTimeMillis();

        countPerSec = iterations*1000L / (end - start);
        System.out.println(format("cryptoGenericHash: %d/sec", countPerSec));


        start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++)
            SodiumLibrary.increment(input);
        end = System.currentTimeMillis();

        countPerSec = iterations*1000L / (end - start);
        System.out.println(format("increment: %d/sec", countPerSec));


        start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++)
            SodiumLibrary.cryptoPwhashAlgArgon2id13();
        end = System.currentTimeMillis();

        countPerSec = iterations*1000L / (end - start);
        System.out.println(format("cryptoPwhashAlgArgon2id13: %d/sec", countPerSec));
        */
    }


    public static void timing() {
        EncryptionService encryptionService = EncryptionService.getInstance();

        SodiumKeyPair key = SodiumLibrary.cryptoBoxKeyPair();
        byte[] ciphertext = SodiumLibrary.cryptoBoxSeal("test".getBytes(), key.getPublicKey());

        long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++)
            SodiumLibrary.cryptoBoxSealOpen(ciphertext, key.getPublicKey(), key.getPrivateKey());
        long end = System.currentTimeMillis();

        long countPerSec = 10000*1000 / (end - start);
        System.out.println(format("Successful decrypts: %d/sec", countPerSec));


        ciphertext[2] = 0;
        start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++) {
            try {
                SodiumLibrary.cryptoBoxSealOpen(ciphertext, key.getPublicKey(), key.getPrivateKey());
                System.out.println("Expected exception");
            } catch(SodiumLibraryException e) {}
        }
        end = System.currentTimeMillis();

        countPerSec = 10000*1000 / (end - start);
        System.out.println(format("Failed decrypts: %d/sec", countPerSec));


        KeyPair key2 = new KeyPair(key);
        byte[] salt = {32, 89, -42, 120};
        start = System.currentTimeMillis();
        for(int i = 0; i < 200000; i++) {
            encryptionService.getShortPublicKeyHash(salt, key2.getPublicKey());
        }
        end = System.currentTimeMillis();

        countPerSec = 200000*1000 / (end - start);
        System.out.println(format(" Short hashes: %d/sec", countPerSec));
    }


    public static void iniTest() {

    }


    public static void hashMapTest() {
        int i = 0;
        int elementCount = 20000;
        int addBatch = elementCount / 10;
        int getCount = addBatch;
        Map<Foo, String> foos = new ConcurrentHashMap<>();//new HashMap<>();
        Random random = new Random();

        long start = System.currentTimeMillis();

        for(i = 0; i < addBatch; i++) {
            foos.put(new Foo(i), format("foo%d", i));
        }

        long end = System.currentTimeMillis();
        System.out.println(end - start);


        start = System.currentTimeMillis();

        for(i = addBatch; i < elementCount; i++) {
            foos.put(new Foo(i), format("foo%d", i));
        }

        end = System.currentTimeMillis();
        System.out.println(end - start);


        start = System.currentTimeMillis();

        for(i = elementCount; i < elementCount + addBatch; i++) {
            foos.put(new Foo(i), format("foo%d", i));
        }

        end = System.currentTimeMillis();
        System.out.println(end - start);


        start = System.currentTimeMillis();

        for(i = 0; i < getCount; i++) {
            foos.get(new Foo(random.nextInt(elementCount)));
        }

        end = System.currentTimeMillis();
        System.out.println(end - start);


        for(i = elementCount + addBatch; i < elementCount * 2; i++) {
            foos.put(new Foo(i), format("foo%d", i));
        }


        start = System.currentTimeMillis();

        for(i = 0; i < getCount; i++) {
            foos.get(new Foo(random.nextInt(elementCount * 2)));
        }

        end = System.currentTimeMillis();
        System.out.println(end - start);
    }


    public static class Foo implements Comparable<Foo> {
        public int value;

        public Foo(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Foo foo = (Foo) o;

            return value == foo.value;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(Foo o) {
            return value - o.value;
        }
    }
}
