package io.mawhebty.exceptions;

public class OTPNotFoundException extends RuntimeException {
    
    public OTPNotFoundException() {
        super("OTP not found or expired");
    }
    
    public OTPNotFoundException(String message) {
        super(message);
    }
    
    public OTPNotFoundException(Long userId) {
        super("No active OTP found for user ID: " + userId);
    }
}