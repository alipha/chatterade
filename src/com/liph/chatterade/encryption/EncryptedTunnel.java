package com.liph.chatterade.encryption;


import com.liph.chatterade.encryption.models.Nonce;
import com.liph.chatterade.encryption.models.SodiumKxKeyPair;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class EncryptedTunnel implements AutoCloseable {
    private final DataInputStream reader;
    private final DataOutputStream writer;

    private Nonce nextReceivingNonce;
    private Nonce nextTransmittingNonce;

    private final SodiumKxKeyPair symmetricKeys;


    public EncryptedTunnel(boolean isClient, InputStream input, OutputStream output) {
        this.reader = new DataInputStream(input);
        this.writer = new DataOutputStream(output);

        this.nextReceivingNonce = new Nonce();
        this.nextTransmittingNonce = new Nonce();

        this.symmetricKeys = EncryptionService.getInstance().performKeyExchange(isClient, this::writeBytes, this::readBytes);
    }


    public void send(byte[] message) {
        byte[] tunneledMessage = EncryptionService.getInstance().encryptMessage(message, nextTransmittingNonce, symmetricKeys.getTransmittingKey());
        nextTransmittingNonce.increment();

        try {
            synchronized (writer) {
                writer.writeShort(tunneledMessage.length);
                writer.write(tunneledMessage);
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] read() {
        try {
            short length = reader.readShort();

            byte[] tunneledMessage = new byte[length];
            reader.readFully(tunneledMessage);

            byte[] message = EncryptionService.getInstance().decryptMessage(tunneledMessage, nextReceivingNonce, symmetricKeys.getReceivingKey());
            nextReceivingNonce.increment();
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws Exception {
        try {
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private void writeBytes(byte[] bytes) {
        try {
            writer.write(bytes);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readBytes(int count) {
        try {
            byte[] bytes = new byte[count];
            reader.readFully(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
