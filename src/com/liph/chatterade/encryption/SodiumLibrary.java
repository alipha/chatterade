//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
// Code forked from https://github.com/muquit/libsodium-jna in order to fix some method calls
// and add missing functionality

/*
License is MIT

Copyright © 2018-2019 muquit@muquit.com

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.liph.chatterade.encryption;

import com.liph.chatterade.encryption.exceptions.SodiumLibraryException;
import com.liph.chatterade.encryption.models.SodiumKxKeyPair;
import com.muquit.libsodiumjna.SodiumKeyPair;
import com.muquit.libsodiumjna.SodiumSecretBox;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SodiumLibrary {
    private static final Logger logger = LoggerFactory.getLogger(SodiumLibrary.class);
    private static String libPath;

    private SodiumLibrary() {
    }

    public static void log(String msg) {
        System.out.println("MMMM: " + msg);
    }

    public static void setLibraryPath(String libraryPath) {
        libPath = libraryPath;
    }

    public static String getLibaryPath() {
        return libPath;
    }

    public static SodiumLibrary.Sodium sodium() {
        if(libPath == null) {
            logger.info("libpath not set, throw exception");
            throw new RuntimeException("Please set the absolute path of the libsodium libary by calling SodiumLibrary.setLibraryPath(path)");
        } else {
            SodiumLibrary.Sodium sodium = SodiumLibrary.SingletonHelper.instance;
            String h = Integer.toHexString(System.identityHashCode(sodium));
            int rc = sodium.sodium_init();
            if(rc == -1) {
                logger.error("ERROR: sodium_init() failed: " + rc);
                throw new RuntimeException("sodium_init() failed, rc=" + rc);
            } else {
                return sodium;
            }
        }
    }

    public static byte[] cryptoSignOpen(byte[] sig, byte[] pk) {
        byte[] m = new byte[sig.length];
        byte[] mlen = new byte[1];
        int rc = sodium().crypto_sign_open(m, (long)mlen[0], sig, (long)sig.length, pk);
        return rc == 0?m:new byte[1];
    }

    public static byte[] cryptoSign(byte[] m, byte[] sk) {
        byte[] sm = new byte[sodium().crypto_sign_bytes().intValue() + m.length];
        byte[] test = new byte[1];
        int rc = sodium().crypto_sign(sm, (long)test[0], m, (long)m.length, sk);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_sign (combined mode, not detached) failed, returned " + rc + ", expected 0");
        } else {
            return sm;
        }
    }

    public static boolean cryptoSignVerifyDetached(byte[] sig, byte[] msg, byte[] pk) {
        int rc = sodium().crypto_sign_verify_detached(sig, msg, (long)msg.length, pk);
        if(rc == 0) {
            return true;
        } else if(rc == -1) {
            return false;
        } else {
            throw new SodiumLibraryException("libsodium crypto_sign_verify_detached failed, returned " + rc + ", expected 0 (a match) or -1 (mismatched)");
        }
    }

    public static byte[] cryptoSignDetached(byte[] msg, byte[] sk) {
        byte[] sig = new byte[sodium().crypto_sign_ed25519_bytes().intValue()];
        long[] siglen = new long[1];
        siglen[0] = sig.length;

        int rc = sodium().crypto_sign_detached(sig, siglen, msg, (long)msg.length, sk);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_sign_detached failed, returned " + rc + ", expected 0");
        } else {
            return sig;
        }
    }

    public static SodiumKeyPair cryptoSignKeyPair() {
        SodiumKeyPair kp = new SodiumKeyPair();
        byte[] publicKey = new byte[sodium().crypto_sign_publickeybytes().intValue()];
        byte[] privateKey = new byte[sodium().crypto_sign_secretkeybytes().intValue()];
        int rc = sodium().crypto_sign_keypair(publicKey, privateKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_sign_keypair() failed, returned " + rc + ", expected 0");
        } else {
            kp.setPublicKey(publicKey);
            kp.setPrivateKey(privateKey);
            logger.info("pk len: " + publicKey.length);
            logger.info("sk len: " + privateKey.length);
            return kp;
        }
    }

    public static byte[] cryptoGenerichash(byte[] input, int length, byte[] key) {
        byte[] hash = new byte[length];
        int keyLen = key != null ? key.length : 0;
        int rc = sodium().crypto_generichash(hash, length, input, input.length, key, keyLen);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_generichash failed, returned " + rc + ", expected 0");
        } else {
            return hash;
        }
    }

    public static byte[] cryptoSignEdSkTOcurveSk(byte[] edSK) {
        byte[] curveSK = new byte[sodium().crypto_box_publickeybytes().intValue()];
        int rc = sodium().crypto_sign_ed25519_sk_to_curve25519(curveSK, edSK);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_generichash failed, returned " + rc + ", expected 0");
        } else {
            return curveSK;
        }
    }

    public static byte[] cryptoSignEdPkTOcurvePk(byte[] edPK) {
        byte[] curvePK = new byte[sodium().crypto_box_publickeybytes().intValue()];
        int rc = sodium().crypto_sign_ed25519_pk_to_curve25519(curvePK, edPK);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_generichash failed, returned " + rc + ", expected 0");
        } else {
            return curvePK;
        }
    }

    public static String libsodiumVersionString() {
        return sodium().sodium_version_string();
    }

    public static byte[] randomBytes(int size) {
        byte[] buf = new byte[size];
        sodium().randombytes_buf(buf, size);
        return buf;
    }

    public static void increment(byte[] nonce) {
        sodium().sodium_increment(nonce, new NativeLong(nonce.length));
    }

    public static byte[] cryptoPwhash(byte[] passwd, byte[] salt, long opsLimit, NativeLong memLimit, int algorithm) {
        byte[] key = new byte[sodium().crypto_box_seedbytes().intValue()];
        logger.info(">>> NavtiveLong size: " + NativeLong.SIZE * 8 + " bits");
        int rc = sodium().crypto_pwhash(key, (long)key.length, passwd, (long)passwd.length, salt, opsLimit, memLimit, algorithm);
        logger.info("crypto_pwhash returned: " + rc);
        if(rc != 0) {
            throw new SodiumLibraryException("cryptoPwhash libsodium crypto_pwhash failed, returned " + rc + ", expected 0");
        } else {
            return key;
        }
    }

    public static byte[] cryptoPwhashArgon2i(byte[] passwd, byte[] salt) {
        int saltLength = cryptoPwhashSaltBytes();
        if(salt.length != saltLength) {
            throw new SodiumLibraryException("salt is " + salt.length + ", it must be" + saltLength + " bytes");
        } else {
            byte[] key = new byte[sodium().crypto_box_seedbytes().intValue()];
            logger.info(">>> NavtiveLong size: " + NativeLong.SIZE * 8 + " bits");
            logger.info(">>> opslimit: " + sodium().crypto_pwhash_opslimit_interactive());
            logger.info(">>> memlimit: " + sodium().crypto_pwhash_memlimit_interactive());
            logger.info(">>> alg: " + sodium().crypto_pwhash_alg_argon2id13());
            int rc = sodium().crypto_pwhash(key, (long)key.length, passwd, (long)passwd.length, salt, sodium().crypto_pwhash_opslimit_interactive().longValue(), sodium().crypto_pwhash_memlimit_interactive(), sodium().crypto_pwhash_alg_argon2id13());
            logger.info("crypto_pwhash returned: " + rc);
            if(rc != 0) {
                throw new SodiumLibraryException("cryptoPwhashArgon2i libsodium crypto_pwhash failed, returned " + rc + ", expected 0");
            } else {
                return key;
            }
        }
    }

    public static byte[] deriveKey(byte[] passwd, byte[] salt) {
        return cryptoPwhashArgon2i(passwd, salt);
    }

    public static String cryptoPwhashStr(byte[] password) {
        byte[] hashedPassword = new byte[sodium().crypto_pwhash_strbytes().intValue()];
        int rc = sodium().crypto_pwhash_str(hashedPassword, password, (long)password.length, sodium().crypto_pwhash_opslimit_interactive().longValue(), sodium().crypto_pwhash_memlimit_interactive());
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_pwhash_str failed, returned " + rc + ", expected 0");
        } else {
            String usAscii = new String(hashedPassword, StandardCharsets.US_ASCII);
            return usAscii;
        }
    }

    public static boolean cryptoPwhashStrVerify(String usAsciiKey, byte[] password) {
        byte[] keyBytes = usAsciiKey.getBytes(StandardCharsets.US_ASCII);
        int rc = sodium().crypto_pwhash_str_verify(keyBytes, password, (long)password.length);
        return rc == 0;
    }

    public static byte[] cryptoPwhashScrypt(byte[] passwd, byte[] salt) {
        NativeLong salt_length = sodium().crypto_pwhash_scryptsalsa208sha256_saltbytes();
        if(salt.length != salt_length.intValue()) {
            throw new SodiumLibraryException("salt is " + salt.length + ", it must be" + salt_length + " bytes");
        } else {
            byte[] key = new byte[sodium().crypto_box_seedbytes().intValue()];
            int rc = sodium().crypto_pwhash_scryptsalsa208sha256(key, (long)key.length, passwd, (long)passwd.length, salt, sodium().crypto_pwhash_opslimit_interactive().longValue(), sodium().crypto_pwhash_memlimit_interactive());
            logger.info("crypto_pwhash_scryptsalsa208sha256 returned: " + rc);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_pwhash_scryptsalsa208sha256() failed, returned " + rc + ", expected 0");
            } else {
                return key;
            }
        }
    }

    public static byte[] cryptoPwhashScryptSalsa208Sha256(byte[] passwd, byte[] salt, Long opsLimit, NativeLong memLimit) {
        NativeLong salt_length = sodium().crypto_pwhash_scryptsalsa208sha256_saltbytes();
        if(salt.length != salt_length.intValue()) {
            throw new SodiumLibraryException("salt is " + salt.length + " bytes, it must be " + salt_length + " bytes");
        } else {
            byte[] key = new byte[sodium().crypto_box_seedbytes().intValue()];
            int rc = sodium().crypto_pwhash_scryptsalsa208sha256(key, (long)key.length, passwd, (long)passwd.length, salt, opsLimit.longValue(), memLimit);
            logger.info("crypto_pwhash_scryptsalsa208sha256 returned: " + rc);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_pwhash_scryptsalsa208sha256() failed, returned " + rc + ", expected 0");
            } else {
                return key;
            }
        }
    }

    public static byte[] cryptoShortHashKeygen() {
        byte[] key = new byte[cryptoShortHashKeyBytes()];
        sodium().crypto_shorthash_keygen(key);
        return key;
    }

    public static byte[] cryptoShortHash(byte[] message, byte[] key) {
        int keyLen = cryptoShortHashKeyBytes();
        if(key.length != keyLen)
            throw new SodiumLibraryException("key is " + key.length + " bytes, it must be " + keyLen + " bytes");

        byte[] hash = new byte[cryptoShortHashBytes()];
        int rc = sodium().crypto_shorthash(hash, message, message.length, key);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_shorthash() failed, returned " + rc + ", expected 0");
        } else {
            return hash;
        }
    }

    public static byte[] cryptoSecretBoxEasy(byte[] message, byte[] nonce, byte[] key) {
        int nonce_length = sodium().crypto_secretbox_noncebytes().intValue();
        if(nonce_length != nonce.length) {
            throw new SodiumLibraryException("nonce is " + nonce.length + ", it must be" + nonce_length + " bytes");
        } else {
            byte[] cipherText = new byte[sodium().crypto_box_macbytes().intValue() + message.length];
            int rc = sodium().crypto_secretbox_easy(cipherText, message, (long)message.length, nonce, key);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_secretbox_easy() failed, returned " + rc + ", expected 0");
            } else {
                return cipherText;
            }
        }
    }

    public static byte[] cryptoSecretBoxOpenEasy(byte[] cipherText, byte[] nonce, byte[] key) {
        if(key.length != sodium().crypto_secretbox_keybytes().intValue()) {
            throw new SodiumLibraryException("invalid key length " + key.length + " bytes");
        } else if(nonce.length != sodium().crypto_secretbox_noncebytes().intValue()) {
            throw new SodiumLibraryException("invalid nonce length " + nonce.length + " bytes");
        } else {
            byte[] decrypted = new byte[cipherText.length - sodium().crypto_box_macbytes().intValue()];
            int rc = sodium().crypto_secretbox_open_easy(decrypted, cipherText, (long)cipherText.length, nonce, key);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_secretbox_open_easy() failed, returned " + rc + ", expected 0");
            } else {
                return decrypted;
            }
        }
    }

    public static SodiumSecretBox cryptoSecretBoxDetached(byte[] message, byte[] nonce, byte[] key) {
        if(key.length != sodium().crypto_secretbox_keybytes().intValue()) {
            throw new SodiumLibraryException("invalid key length " + key.length + " bytes");
        } else if(nonce.length != sodium().crypto_secretbox_noncebytes().intValue()) {
            throw new SodiumLibraryException("invalid nonce length " + nonce.length + " bytes");
        } else {
            byte[] cipherText = new byte[message.length];
            byte[] mac = new byte[sodium().crypto_secretbox_macbytes().intValue()];
            int rc = sodium().crypto_secretbox_detached(cipherText, mac, message, (long)message.length, nonce, key);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_secretbox_detached() failed, returned " + rc + ", expected 0");
            } else {
                SodiumSecretBox secretBox = new SodiumSecretBox();
                secretBox.setCipherText(cipherText);
                secretBox.setMac(mac);
                return secretBox;
            }
        }
    }

    public static byte[] cryptoSecretBoxOpenDetached(SodiumSecretBox secretBox, byte[] nonce, byte[] key) {
        if(key.length != sodium().crypto_secretbox_keybytes().intValue()) {
            throw new SodiumLibraryException("invalid key length " + key.length + " bytes");
        } else if(nonce.length != sodium().crypto_secretbox_noncebytes().intValue()) {
            throw new SodiumLibraryException("invalid nonce length " + nonce.length + " bytes");
        } else {
            byte[] mac = secretBox.getMac();
            if(mac.length != sodium().crypto_secretbox_macbytes().intValue()) {
                throw new SodiumLibraryException("invalid mac length " + mac.length + " bytes");
            } else {
                byte[] message = new byte[secretBox.getCipherText().length];
                byte[] cipherText = secretBox.getCipherText();
                int rc = sodium().crypto_secretbox_open_detached(message, cipherText, mac, (long)cipherText.length, nonce, key);
                if(rc != 0) {
                    throw new SodiumLibraryException("libsodium crypto_secretbox_open_detached() failed, returned " + rc + ", expected 0");
                } else {
                    return message;
                }
            }
        }
    }

    public static byte[] cryptoAuth(byte[] message, byte[] key) {
        byte[] mac = new byte[sodium().crypto_auth_bytes().intValue()];
        int keySize = sodium().crypto_auth_keybytes().intValue();
        if(key.length != keySize) {
            throw new SodiumLibraryException("Expected key size " + keySize + " bytes, but passed " + key.length + " bytes");
        } else {
            int rc = sodium().crypto_auth(mac, message, (long)message.length, key);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_auth() failed, returned " + rc + ", expected 0");
            } else {
                return mac;
            }
        }
    }

    public static boolean cryptoAuthVerify(byte[] mac, byte[] message, byte[] key) {
        int keySize = sodium().crypto_auth_keybytes().intValue();
        if(key.length != keySize) {
            throw new SodiumLibraryException("Expected key size " + keySize + " bytes, but passed " + key.length + " bytes");
        } else {
            int rc = sodium().crypto_auth_verify(mac, message, (long)message.length, key);
            return rc == 0?true:(rc == -1?false:false);
        }
    }

    public static SodiumKeyPair cryptoBoxKeyPair() {
        SodiumKeyPair kp = new SodiumKeyPair();
        byte[] publicKey = new byte[sodium().crypto_box_publickeybytes().intValue()];
        byte[] privateKey = new byte[sodium().crypto_box_secretkeybytes().intValue()];
        int rc = sodium().crypto_box_keypair(publicKey, privateKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_box_keypair() failed, returned " + rc + ", expected 0");
        } else {
            kp.setPublicKey(publicKey);
            kp.setPrivateKey(privateKey);
            logger.info("pk len: " + publicKey.length);
            logger.info("sk len: " + privateKey.length);
            return kp;
        }
    }

    public static byte[] cryptoPublicKey(byte[] privateKey) {
        byte[] publicKey = new byte[sodium().crypto_box_publickeybytes().intValue()];
        int rc = sodium().crypto_scalarmult_base(publicKey, privateKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_scalrmult() failed, returned " + rc + ", expected 0");
        } else {
            return publicKey;
        }
    }


    public static SodiumKeyPair cryptoKxKeyPair() {
        byte[] publicKey = new byte[sodium().crypto_kx_publickeybytes().intValue()];
        byte[] privateKey = new byte[sodium().crypto_kx_secretkeybytes().intValue()];

        int rc = sodium().crypto_kx_keypair(publicKey, privateKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_kx_keypair() failed, returned " + rc + ", expected 0");
        } else {
            return new SodiumKeyPair(publicKey, privateKey);
        }
    }

    public static SodiumKeyPair cryptoKxSeedKeyPair(byte[] seed) {
        byte[] publicKey = new byte[sodium().crypto_kx_publickeybytes().intValue()];
        byte[] privateKey = new byte[sodium().crypto_kx_secretkeybytes().intValue()];
        int seedLen = sodium().crypto_kx_seedbytes().intValue();

        if(seed.length != seedLen)
            throw new SodiumLibraryException("Expected seed size " + seedLen + " bytes for cryptoKxSeedKeyPair seed, but passed " + seed.length + " bytes");

        int rc = sodium().crypto_kx_seed_keypair(publicKey, privateKey, seed);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_kx_seed_keypair() failed, returned " + rc + ", expected 0");
        } else {
            return new SodiumKeyPair(publicKey, privateKey);
        }
    }

    public static SodiumKxKeyPair cryptoKxClientSessionKeys(byte[] clientPublicKey, byte[] clientSecretKey, byte[] serverPublicKey) {
        byte[] receivingKey = new byte[sodium().crypto_kx_sessionkeybytes().intValue()];
        byte[] transmittingKey = new byte[sodium().crypto_kx_sessionkeybytes().intValue()];
        int publicKeyLen = sodium().crypto_kx_publickeybytes().intValue();
        int secretKeyLen = sodium().crypto_kx_secretkeybytes().intValue();

        if(clientPublicKey.length != publicKeyLen)
            throw new SodiumLibraryException("Expected key size " + publicKeyLen + " bytes for cryptoKxClientSessionKeys clientPublicKey, but passed " + clientPublicKey.length + " bytes");
        if(clientSecretKey.length != secretKeyLen)
            throw new SodiumLibraryException("Expected key size " + secretKeyLen + " bytes for cryptoKxClientSessionKeys clientSecretKey, but passed " + clientSecretKey.length + " bytes");
        if(serverPublicKey.length != publicKeyLen)
            throw new SodiumLibraryException("Expected key size " + publicKeyLen + " bytes for cryptoKxClientSessionKeys serverPublicKey, but passed " + serverPublicKey.length + " bytes");

        int rc = sodium().crypto_kx_client_session_keys(receivingKey, transmittingKey, clientPublicKey, clientSecretKey, serverPublicKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_kx_client_session_keys() failed, returned " + rc + ", expected 0");
        } else {
            return new SodiumKxKeyPair(receivingKey, transmittingKey);
        }
    }

    public static SodiumKxKeyPair cryptoKxServerSessionKeys(byte[] serverPublicKey, byte[] serverSecretKey, byte[] clientPublicKey) {
        byte[] receivingKey = new byte[sodium().crypto_kx_sessionkeybytes().intValue()];
        byte[] transmittingKey = new byte[sodium().crypto_kx_sessionkeybytes().intValue()];
        int publicKeyLen = sodium().crypto_kx_publickeybytes().intValue();
        int secretKeyLen = sodium().crypto_kx_secretkeybytes().intValue();

        if(serverPublicKey.length != publicKeyLen)
            throw new SodiumLibraryException("Expected key size " + publicKeyLen + " bytes for cryptoKxServerSessionKeys serverPublicKey, but passed " + serverPublicKey.length + " bytes");
        if(serverSecretKey.length != secretKeyLen)
            throw new SodiumLibraryException("Expected key size " + secretKeyLen + " bytes for cryptoKxServerSessionKeys serverSecretKey, but passed " + serverSecretKey.length + " bytes");
        if(clientPublicKey.length != publicKeyLen)
            throw new SodiumLibraryException("Expected key size " + publicKeyLen + " bytes for cryptoKxServerSessionKeys clientPublicKey, but passed " + clientPublicKey.length + " bytes");

        int rc = sodium().crypto_kx_server_session_keys(receivingKey, transmittingKey, serverPublicKey, serverSecretKey, clientPublicKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_kx_server_session_keys() failed, returned " + rc + ", expected 0");
        } else {
            return new SodiumKxKeyPair(receivingKey, transmittingKey);
        }
    }


    public static NativeLong cryptoBoxNonceBytes() {
        return sodium().crypto_box_noncebytes();
    }

    public static NativeLong crytoBoxSeedBytes() {
        return sodium().crypto_box_seedbytes();
    }

    public static NativeLong crytoBoxPublicKeyBytes() {
        return sodium().crypto_box_publickeybytes();
    }

    public static NativeLong crytoBoxSecretKeyBytes() {
        return sodium().crypto_box_secretkeybytes();
    }

    public static NativeLong cryptoBoxMacBytes() {
        return sodium().crypto_box_macbytes();
    }

    public static NativeLong cryptoBoxSealBytes() {
        return sodium().crypto_box_sealbytes();
    }

    public static NativeLong crytoKxSeedBytes() {
        return sodium().crypto_kx_seedbytes();
    }

    public static NativeLong crytoKxPublicKeyBytes() {
        return sodium().crypto_kx_publickeybytes();
    }

    public static NativeLong crytoKxSecretKeyBytes() {
        return sodium().crypto_kx_secretkeybytes();
    }

    public static NativeLong cryptoKxSessionKeyBytes() {
        return sodium().crypto_kx_sessionkeybytes();
    }

    public static NativeLong cryptoSecretBoxKeyBytes() {
        return sodium().crypto_secretbox_keybytes();
    }

    public static NativeLong cryptoSecretBoxNonceBytes() {
        return sodium().crypto_secretbox_noncebytes();
    }

    public static NativeLong cryptoSecretBoxMacBytes() {
        return sodium().crypto_secretbox_macbytes();
    }

    public static int cryptoNumberSaltBytes() {
        return sodium().crypto_pwhash_saltbytes().intValue();
    }

    public static int cryptoPwhashAlgArgon2i13() {
        return sodium().crypto_pwhash_alg_argon2i13();
    }

    public static int cryptoPwhashAlgArgon2id13() {
        return sodium().crypto_pwhash_alg_argon2id13();
    }

    public static int cryptoPwhashAlgDefault() {
        return sodium().crypto_pwhash_alg_default();
    }

    public static int cryptoPwhashSaltBytes() {
        return sodium().crypto_pwhash_saltbytes().intValue();
    }

    public static long cryptoPwHashOpsLimitInteractive() {
        return sodium().crypto_pwhash_opslimit_interactive().longValue();
    }

    public static long cryptoPwHashMemLimitInterative() {
        return sodium().crypto_pwhash_memlimit_interactive().longValue();
    }

    public static int cryptoPwHashScryptSalsa208Sha256SaltBytes() {
        return sodium().crypto_pwhash_scryptsalsa208sha256_saltbytes().intValue();
    }

    public static int cryptoShortHashKeyBytes() {
        return sodium().crypto_shorthash_keybytes().intValue();
    }
    
    public static int cryptoShortHashBytes() {
        return sodium().crypto_shorthash_bytes().intValue();
    }

    public static byte[] cryptoBoxEasy(byte[] message, byte[] nonce, byte[] publicKey, byte[] privateKey) {
        NativeLong nonce_len = sodium().crypto_box_noncebytes();
        if(nonce.length != nonce_len.intValue()) {
            throw new SodiumLibraryException("nonce is " + nonce.length + "bytes, it must be" + nonce_len + " bytes");
        } else {
            byte[] cipherText = new byte[sodium().crypto_box_macbytes().intValue() + message.length];
            int rc = sodium().crypto_box_easy(cipherText, message, (long)message.length, nonce, publicKey, privateKey);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_box_easy() failed, returned " + rc + ", expected 0");
            } else {
                return cipherText;
            }
        }
    }

    public static byte[] cryptoBoxOpenEasy(byte[] cipherText, byte[] nonce, byte[] publicKey, byte[] privateKey) {
        NativeLong nonce_len = sodium().crypto_box_noncebytes();
        if(nonce.length != nonce_len.intValue()) {
            throw new SodiumLibraryException("nonce is " + nonce.length + "bytes, it must be" + nonce_len + " bytes");
        } else {
            byte[] decrypted = new byte[cipherText.length - sodium().crypto_box_macbytes().intValue()];
            int rc = sodium().crypto_box_open_easy(decrypted, cipherText, (long)cipherText.length, nonce, publicKey, privateKey);
            if(rc != 0) {
                throw new SodiumLibraryException("libsodium crypto_box_open_easy() failed, returned " + rc + ", expected 0");
            } else {
                return decrypted;
            }
        }
    }

    public static byte[] cryptoBoxSeal(byte[] message, byte[] recipientPublicKey) {
        logger.info("message len: " + message.length);
        byte[] cipherText = new byte[sodium().crypto_box_sealbytes().intValue() + message.length];
        int rc = sodium().crypto_box_seal(cipherText, message, (long)message.length, recipientPublicKey);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_box_seal() failed, returned " + rc + ", expected 0");
        } else {
            return cipherText;
        }
    }

    public static byte[] cryptoBoxSealOpen(byte[] cipherText, byte[] pk, byte[] sk) {
        byte[] decrypted = new byte[cipherText.length - sodium().crypto_box_sealbytes().intValue()];
        int rc = sodium().crypto_box_seal_open(decrypted, cipherText, (long)cipherText.length, pk, sk);
        if(rc != 0) {
            throw new SodiumLibraryException("libsodium crypto_box_seal_open() failed, returned " + rc + ", expected 0");
        } else {
            return decrypted;
        }
    }

    public interface Sodium extends Library {
        int sodium_library_version_major();

        int sodium_library_version_minor();

        int sodium_init();

        String sodium_version_string();

        void randombytes_buf(byte[] var1, int var2);

        void sodium_increment(byte[] n, NativeLong len);

        int crypto_pwhash_alg_argon2i13();

        int crypto_pwhash_alg_argon2id13();

        int crypto_pwhash_alg_default();

        NativeLong crypto_pwhash_saltbytes();

        NativeLong crypto_pwhash_strbytes();

        Pointer crypto_pwhash_strprefix();

        NativeLong crypto_pwhash_opslimit_interactive();

        NativeLong crypto_pwhash_memlimit_interactive();

        NativeLong crypto_pwhash_opslimit_moderate();

        NativeLong crypto_pwhash_memlimit_moderate();

        NativeLong crypto_pwhash_opslimit_sensitive();

        NativeLong crypto_pwhash_memlimit_sensitive();

        NativeLong crypto_shorthash_keybytes();

        NativeLong crypto_shorthash_bytes();

        NativeLong crypto_box_seedbytes();

        NativeLong crypto_box_publickeybytes();

        NativeLong crypto_box_secretkeybytes();

        NativeLong crypto_box_noncebytes();

        NativeLong crypto_box_macbytes();

        NativeLong crypto_box_sealbytes();

        NativeLong crypto_auth_bytes();

        NativeLong crypto_auth_keybytes();

        NativeLong crypto_kx_publickeybytes();

        NativeLong crypto_kx_secretkeybytes();

        NativeLong crypto_kx_seedbytes();

        NativeLong crypto_kx_sessionkeybytes();

        int crypto_pwhash(byte[] var1, long var2, byte[] var4, long var5, byte[] var7, long var8, NativeLong var10, int var11);

        int crypto_pwhash_scryptsalsa208sha256(byte[] var1, long var2, byte[] var4, long var5, byte[] var7, long var8, NativeLong var10);

        int crypto_pwhash_str(byte[] var1, byte[] var2, long var3, long var5, NativeLong var7);

        int crypto_pwhash_str_verify(byte[] var1, byte[] var2, long var3);

        void crypto_shorthash_keygen(byte[] key);

        int crypto_shorthash(byte[] out, byte[] in, long inlen, byte[] k);

        NativeLong crypto_pwhash_scryptsalsa208sha256_saltbytes();

        NativeLong crypto_secretbox_keybytes();

        NativeLong crypto_secretbox_noncebytes();

        NativeLong crypto_secretbox_macbytes();

        int crypto_secretbox_easy(byte[] var1, byte[] var2, long var3, byte[] var5, byte[] var6);

        int crypto_secretbox_open_easy(byte[] var1, byte[] var2, long var3, byte[] var5, byte[] var6);

        int crypto_secretbox_detached(byte[] var1, byte[] var2, byte[] var3, long var4, byte[] var6, byte[] var7);

        int crypto_secretbox_open_detached(byte[] var1, byte[] var2, byte[] var3, long var4, byte[] var6, byte[] var7);

        int crypto_box_seal(byte[] var1, byte[] var2, long var3, byte[] var5);

        int crypto_box_seal_open(byte[] var1, byte[] var2, long var3, byte[] var5, byte[] var6);

        int crypto_auth(byte[] var1, byte[] var2, long var3, byte[] var5);

        int crypto_auth_verify(byte[] var1, byte[] var2, long var3, byte[] var5);

        int crypto_box_keypair(byte[] var1, byte[] var2);

        int crypto_scalarmult_base(byte[] var1, byte[] var2);

        int crypto_kx_keypair(byte[] pk, byte[] sk);

        int crypto_kx_seed_keypair(byte[] pk, byte[] sk, byte[] seed);

        int crypto_kx_client_session_keys(byte[] rx, byte[] tx, byte[] client_pk, byte[] client_sk, byte[] server_pk);

        int crypto_kx_server_session_keys(byte[] rx, byte[] tx, byte[] server_pk, byte[] server_sk, byte[] client_pk);

        int crypto_box_easy(byte[] var1, byte[] var2, long var3, byte[] var5, byte[] var6, byte[] var7);

        int crypto_box_open_easy(byte[] var1, byte[] var2, long var3, byte[] var5, byte[] var6, byte[] var7);

        NativeLong crypto_sign_secretkeybytes();

        NativeLong crypto_sign_publickeybytes();

        int crypto_sign_keypair(byte[] var1, byte[] var2);

        NativeLong crypto_sign_ed25519_bytes();

        NativeLong crypto_sign_bytes();

        int crypto_sign_detached(byte[] var1, long[] var2, byte[] var4, long var5, byte[] var7);

        int crypto_sign_verify_detached(byte[] var1, byte[] var2, long var3, byte[] var5);

        int crypto_sign(byte[] var1, long var2, byte[] var4, long var5, byte[] var7);

        int crypto_sign_open(byte[] var1, long var2, byte[] var4, long var5, byte[] var7);

        int crypto_generichash(byte[] var1, int var2, byte[] var3, long var4, byte[] var5, int var6);

        int crypto_sign_ed25519_sk_to_curve25519(byte[] var1, byte[] var2);

        int crypto_sign_ed25519_pk_to_curve25519(byte[] var1, byte[] var2);
    }

    private static final class SingletonHelper {
        public static final SodiumLibrary.Sodium instance;

        private SingletonHelper() {
        }

        static {
            instance = (SodiumLibrary.Sodium)Native.loadLibrary(SodiumLibrary.libPath, SodiumLibrary.Sodium.class);
        }
    }
}
