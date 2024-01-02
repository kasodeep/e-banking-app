package dev.deep.banking.exception;

public class AccountNotClearedException extends RuntimeException{
    public AccountNotClearedException(String message){
        super(message);
    }
}
