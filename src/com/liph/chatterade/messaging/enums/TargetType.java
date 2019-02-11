package com.liph.chatterade.messaging.enums;

import java.util.Arrays;
import java.util.List;


public enum TargetType {
    NONE,
    CHANNEL,
    USER,
    CHANNEL_OR_USER  (CHANNEL, USER),
    MULTIPLE_CHANNELS(CHANNEL),
    INVALID;
    
    private final List<TargetType> children;
    
    
    TargetType(TargetType... children) {
        this.children = Arrays.asList(children);
    }
    
    public boolean includes(TargetType child) {
        return this == child || children.contains(child);
    }
}
