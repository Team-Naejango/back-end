package com.example.naejango.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(of = {"id", "accessToken", "refreshToken"})
@Builder
public class LoginResponse {
    private Long id;
    private String accessToken;
    private String refreshToken;
}
