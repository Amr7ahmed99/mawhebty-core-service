package io.mawhebty.exceptions;

public class UserNotVerified extends RuntimeException{
    
    public UserNotVerified(){
        super("User not verified");
    }

    public UserNotVerified(String message){
        super(message);
    }

}
