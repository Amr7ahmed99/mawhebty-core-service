package io.mawhebty.exceptions;

public class PhoneAlreadyExistsException extends RuntimeException{
    public PhoneAlreadyExistsException() {
        super("user phone is already exist");
    }
    
    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
}
