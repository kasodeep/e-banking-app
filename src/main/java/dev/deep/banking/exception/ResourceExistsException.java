package dev.deep.banking.exception;

public class ResourceExistsException extends RuntimeException{

    public ResourceExistsException(String message) {
        super(message);
    }
}
