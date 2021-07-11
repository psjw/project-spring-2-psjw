package com.psjw.thisbox.exceptions;

public class LoginFailException extends RuntimeException{
    public LoginFailException(String message) {
        super("Login Fail : "+message);
    }
}
