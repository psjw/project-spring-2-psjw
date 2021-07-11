package com.psjw.thisbox.exceptions;

public class UserEmailDuplicationException extends RuntimeException{
    public UserEmailDuplicationException(String email) {
        super("Already exists Email : "+email);
    }
}
