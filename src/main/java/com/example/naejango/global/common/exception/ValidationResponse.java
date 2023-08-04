package com.example.naejango.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationResponse {
    private int status;
    private String error;
    private List<ValidationError> exceptions;

    public static ResponseEntity<ValidationResponse> toResponseEntity (MethodArgumentNotValidException e) {
        ValidationResponse response = ValidationResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .exceptions(toValidationErrorList(e.getBindingResult())).build();
        return ResponseEntity.badRequest().body(response);
    }

    private static List<ValidationError> toValidationErrorList(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.stream().map(error ->
                new ValidationError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());
    }

}
