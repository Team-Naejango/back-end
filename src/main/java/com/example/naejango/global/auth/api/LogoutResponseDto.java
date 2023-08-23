package com.example.naejango.global.auth.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LogoutResponseDto {
    private String message;
}
