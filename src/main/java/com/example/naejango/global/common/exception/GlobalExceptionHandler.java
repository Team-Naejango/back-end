package com.example.naejango.global.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorResponse> CustomExceptionHandler(CustomException exception) {
        return ErrorResponse.toResponseEntity(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationResponse> ValidationExceptionHandler(MethodArgumentNotValidException e) {
        return ValidationResponse.toResponseEntity(e);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<TokenErrorResponse> TokenExceptionHandler(TokenException exception) {
        return TokenErrorResponse.toResponseEntity(exception.getErrorCode(), exception.getReissuedAccessToken());
    }

}
