package com.example.naejango.global.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class TokenErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final String reissuedAccessToken;

    public static ResponseEntity<TokenErrorResponse> toHttpResponseEntity(ErrorCode errorCode, String accessToken) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                        .body(
                                TokenErrorResponse.builder()
                                        .status(errorCode.getHttpStatusCode())
                                        .error(errorCode.getHttpStatusErrorName())
                                        .message(errorCode.getMessage())
                                        .reissuedAccessToken(accessToken)
                                        .build()
                        );

    }

    public static TokenErrorResponse toFilterResponseEntity(ErrorCode errorCode, String accessToken) {
        return TokenErrorResponse.builder()
                    .status(errorCode.getHttpStatusCode())
                    .error(errorCode.getHttpStatusErrorName())
                    .message(errorCode.getMessage())
                    .reissuedAccessToken(accessToken)
                    .build();
    }

}
