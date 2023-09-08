package com.example.naejango.global.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> CustomExceptionHandler(CustomException exception) {
        return ErrorResponse.toResponseEntity(exception.getErrorCode());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationResponse> ValidationExceptionHandler(BindException e) {
        return ValidationResponse.toResponseEntity(e);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<TokenErrorResponse> TokenExceptionHandler(TokenException exception) {
        return TokenErrorResponse.toHttpResponseEntity(exception.getErrorCode(), exception.getReissuedAccessToken());
    }

}
