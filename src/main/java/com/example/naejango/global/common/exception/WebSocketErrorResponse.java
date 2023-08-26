package com.example.naejango.global.common.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebSocketErrorResponse {

    private final String error;
    private final String message;

    public static WebSocketErrorResponse response(ErrorCode errorCode) {
        return WebSocketErrorResponse.builder()
                .error(errorCode.getHttpStatusErrorName())
                .message(errorCode.getMessage()).build();
    }

}
