package dev.deep.banking.exception;

public class ValueMismatchException extends RuntimeException{
    public ValueMismatchException(String message){
        super(message);
    }
}
