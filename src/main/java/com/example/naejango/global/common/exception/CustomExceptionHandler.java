package com.example.naejango.global.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler  {

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorResponse> ExceptionHandler(CustomException exception) {
        return ErrorResponse.toResponseEntity(exception.getErrorCode());
    }
}
