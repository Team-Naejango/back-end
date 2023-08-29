package com.example.naejango.global.auth.dto.response;

import com.example.naejango.global.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReissueAccessTokenResponseDto {
    private String message;
    private String accessToken;

    public ReissueAccessTokenResponseDto(ErrorCode errorCode, String accessToken) {
        this.message = errorCode.getMessage();
        this.accessToken = accessToken;
    }
}
