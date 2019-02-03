package com.liph.chatterade.parsing.models;


import com.liph.chatterade.common.ByteArray;
import com.liph.chatterade.messaging.enums.TargetType;
import java.util.Optional;


public class Target {

    private final TargetType targetType;
    private final Optional<String> nick;
    private final Optional<String> channel;
    private final Optional<String> publicKey;


    public Target(TargetType targetType, Optional<String> nick, Optional<String> channel, Optional<String> publicKey) {
        this.targetType = targetType;
        this.nick = nick;
        this.channel = channel;
        this.publicKey = publicKey;
    }


    public TargetType getTargetType() {
        return targetType;
    }

    public Optional<String> getNick() {
        return nick;
    }

    public Optional<String> getChannel() {
        return channel;
    }

    public Optional<String> getPublicKey() {
        return publicKey;
    }

    public Optional<ByteArray> getPublicKeyBytes() {
        return publicKey.map(ByteArray::new);
    }
}
