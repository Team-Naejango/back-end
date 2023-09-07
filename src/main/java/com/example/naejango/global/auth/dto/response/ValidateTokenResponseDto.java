package com.example.naejango.global.auth.dto.response;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenResponseDto {
    private Long userId;
    private boolean isValidToken;
}
