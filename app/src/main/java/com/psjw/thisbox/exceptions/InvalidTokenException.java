package com.psjw.thisbox.exceptions;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String token) {
        super("Invalid Token : "+token);
    }
}
