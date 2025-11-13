package io.mawhebty.exceptions;

public class UserStatusNotFoundException extends RuntimeException {
    
    public UserStatusNotFoundException() {
        super("User status not found");
    }
    
    public UserStatusNotFoundException(String message) {
        super(message);
    }
    
    public UserStatusNotFoundException(Integer statusId) {
        super("Invalid user status with ID: " + statusId);
    }
    
    public UserStatusNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}