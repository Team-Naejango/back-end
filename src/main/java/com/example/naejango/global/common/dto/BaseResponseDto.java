package com.example.naejango.global.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseResponseDto {

    private int status;
    private String message;

    public BaseResponseDto(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
