package com.psjw.thisbox.controllers;

import com.psjw.thisbox.dto.ErrorResponseData;
import com.psjw.thisbox.exceptions.InvalidTokenException;
import com.psjw.thisbox.exceptions.LoginFailException;
import com.psjw.thisbox.exceptions.UserEmailDuplicationException;
import com.psjw.thisbox.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@ResponseBody
@ControllerAdvice
public class ControllerErrorAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ErrorResponseData handleUserNotFoundException(){
        return new ErrorResponseData("User Not found");
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LoginFailException.class)
    public ErrorResponseData handleLoginFailException(){
        return new ErrorResponseData("Login Fail");
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseData handleContraintValidateError(ConstraintViolationException exception){
        String messageTemplate = getViolatedMessage(exception);
        return new ErrorResponseData(messageTemplate);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserEmailDuplicationException.class)
    public ErrorResponseData handleUserEamilAlreayExisted(){
        return new ErrorResponseData("User's Email is already existed");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidTokenException.class)
    public ErrorResponseData handleInvalidAcceesTokenException(){
        return new ErrorResponseData("Invalid Access Token");
    }



    private String getViolatedMessage(ConstraintViolationException exception) {
        String messageTemplate = null;
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            messageTemplate = violation.getMessageTemplate();
        }
        return messageTemplate;
    }
}
