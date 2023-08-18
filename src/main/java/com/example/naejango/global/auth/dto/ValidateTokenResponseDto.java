package com.example.naejango.global.auth.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenResponseDto {
    private Long userId;
    private boolean isValidToken;
}
