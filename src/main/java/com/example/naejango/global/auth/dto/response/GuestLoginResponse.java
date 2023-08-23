package com.example.naejango.global.auth.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GuestLoginResponse {
    private String message;
    private String accessToken;
}
