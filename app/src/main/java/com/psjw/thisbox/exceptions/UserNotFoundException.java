package com.psjw.thisbox.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(Long userId) {
        super("User Not found : "+userId);
    }
}
