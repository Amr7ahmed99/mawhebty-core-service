package io.mawhebty.exceptions;

public class OTPExpiredException extends RuntimeException {
    
    public OTPExpiredException() {
        super("OTP has expired");
    }
    
    public OTPExpiredException(String message) {
        super(message);
    }
}