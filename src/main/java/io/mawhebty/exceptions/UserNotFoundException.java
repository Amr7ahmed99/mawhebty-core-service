package io.mawhebty.exceptions;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException() {
        super("User not found");
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}