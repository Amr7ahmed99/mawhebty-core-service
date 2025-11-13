package io.mawhebty.exceptions;

public class OTPAlreadyUsedException extends RuntimeException {
    
    public OTPAlreadyUsedException() {
        super("OTP has already been used");
    }
    
    public OTPAlreadyUsedException(String message) {
        super(message);
    }
}