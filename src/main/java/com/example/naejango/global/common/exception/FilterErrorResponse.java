package com.example.naejango.global.common.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public static FilterErrorResponse toResponseEntity(ErrorCode errorCode) {
        return FilterErrorResponse.builder()
                .status(errorCode.getHttpStatusCode())
                .error(errorCode.getHttpStatusErrorName())
                .message(errorCode.getMessage())
                .build();
    }
}
