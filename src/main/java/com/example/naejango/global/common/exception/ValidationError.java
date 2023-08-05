package com.example.naejango.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {
    private String field;
    private String value;
    private String message;
}
