package com.liph.chatterade.encryption.exceptions;


public class SodiumLibraryException extends RuntimeException {
    private static final long serialVersionUID = 30349089390987L;
    private String message;

    public SodiumLibraryException() {
        this.message = null;
    }

    public SodiumLibraryException(String message) {
        super(message);
        this.message = message;
    }

    public SodiumLibraryException(Throwable cause) {
        super(cause);
        this.message = cause != null?cause.getMessage():null;
    }

    public SodiumLibraryException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}