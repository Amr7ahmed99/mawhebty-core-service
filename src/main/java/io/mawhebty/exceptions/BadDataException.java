package io.mawhebty.exceptions;

public class BadDataException extends RuntimeException{
    
    public BadDataException(){
        super();
    }

    public BadDataException(String err){
        super(err);
    }
}
