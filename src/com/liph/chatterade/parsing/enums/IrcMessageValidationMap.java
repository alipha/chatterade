package com.liph.chatterade.parsing.enums;

import static com.liph.chatterade.common.enums.RequirementType.DISALLOWED;
import static com.liph.chatterade.common.enums.RequirementType.PERMITTED;
import static com.liph.chatterade.common.enums.RequirementType.REQUIRED;
import static com.liph.chatterade.messaging.enums.TargetType.CHANNEL_OR_USER;
import static com.liph.chatterade.messaging.enums.TargetType.MULTIPLE_CHANNELS;
import static com.liph.chatterade.messaging.enums.TargetType.NONE;

import com.liph.chatterade.common.EnumHelper;
import com.liph.chatterade.common.enums.RequirementType;
import com.liph.chatterade.messaging.enums.TargetType;
import java.util.Optional;


public enum IrcMessageValidationMap {
    JOIN   (0, Optional.of(1), DISALLOWED, MULTIPLE_CHANNELS),
    NICK   (1, Optional.of(1), DISALLOWED, NONE),
    NOTICE (1, Optional.of(1), REQUIRED  , CHANNEL_OR_USER),
    PART   (0, Optional.of(0), DISALLOWED, MULTIPLE_CHANNELS),
    PASS   (1, Optional.of(1), DISALLOWED, NONE),
    PRIVMSG(1, Optional.of(1), REQUIRED  , CHANNEL_OR_USER),
    QUIT   (0, Optional.of(1), PERMITTED , NONE),
    USER   (4, Optional.of(4), REQUIRED  , NONE),
    PING   (1, Optional.of(1), PERMITTED , NONE),
    PONG   (1, Optional.of(1), PERMITTED , NONE);


    private final int minimumArgumentCount;
    private final Optional<Integer> maximumArgumentCount;
    private final RequirementType trailingArgumentRequirement;
    private final TargetType allowedTarget;


    IrcMessageValidationMap(int minArgCount, Optional<Integer> maxArgCount, RequirementType trailingArgument, TargetType allowedTarget) {
        this.minimumArgumentCount = minArgCount;
        this.maximumArgumentCount = maxArgCount;
        this.trailingArgumentRequirement = trailingArgument;
        this.allowedTarget = allowedTarget;
    }


    public int getMinimumArgumentCount() {
        return minimumArgumentCount;
    }

    public Optional<Integer> getMaximumArgumentCount() {
        return maximumArgumentCount;
    }

    public RequirementType getTrailingArgumentRequirement() {
        return trailingArgumentRequirement;
    }

    public TargetType getAllowedTarget() {
        return allowedTarget;
    }


    public boolean hasTarget() {
        return allowedTarget != NONE;
    }

    public boolean isTargetTypeAllowed(TargetType type) {
        return allowedTarget.includes(type);
    }


    public static Optional<IrcMessageValidationMap> fromName(String name) {
        return EnumHelper.fromName(values(), name);
    }
}
