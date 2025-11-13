package io.mawhebty.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException() {
        super("user email is already exist");
    }
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
