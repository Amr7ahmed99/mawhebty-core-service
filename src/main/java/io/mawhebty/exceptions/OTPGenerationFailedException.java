package io.mawhebty.exceptions;

public class OTPGenerationFailedException extends RuntimeException{
    
    public OTPGenerationFailedException() {
        super("OTP generation failed");
    }
    
    public OTPGenerationFailedException(String message) {
        super(message);
    }
}
