package com.example.naejango.domain.storage.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateStorageResponseDto {
    private Long storageId;
    private String message;
}
