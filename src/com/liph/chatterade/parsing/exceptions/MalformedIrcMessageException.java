package com.liph.chatterade.parsing.exceptions;


public class MalformedIrcMessageException extends IllegalArgumentException {

    private final String messageType;
    private final String errorCode;


    public MalformedIrcMessageException(String messageType, String errorCode, String errorMessage) {
        super(errorMessage);
        this.messageType = messageType;
        this.errorCode = errorCode;
    }


    public String getMessageType() {
        return messageType;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
