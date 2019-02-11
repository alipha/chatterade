package com.liph.chatterade.common;

import java.util.Optional;


public class EnumHelper {

    public static <T extends Enum<T>> Optional<T> fromName(T[] enumValues, String name) {

        for(T enumValue : enumValues)
            if(enumValue.name().equalsIgnoreCase(name))
                return Optional.of(enumValue);

        return Optional.empty();
    }
}
