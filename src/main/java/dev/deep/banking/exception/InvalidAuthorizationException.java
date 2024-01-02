package dev.deep.banking.exception;

public class InvalidAuthorizationException extends RuntimeException{

    public InvalidAuthorizationException(String message) {
        super(message);
    }
}
