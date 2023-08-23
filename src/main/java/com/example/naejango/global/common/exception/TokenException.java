package com.example.naejango.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenException extends RuntimeException{
    private ErrorCode errorCode;
    private String reissuedAccessToken;
}
