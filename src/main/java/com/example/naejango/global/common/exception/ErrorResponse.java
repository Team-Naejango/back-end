package com.example.naejango.global.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getHttpStatusCode())
                        .error(errorCode.getHttpStatusErrorName())
                        .message(errorCode.getMessage())
                        .build());
    }

}
