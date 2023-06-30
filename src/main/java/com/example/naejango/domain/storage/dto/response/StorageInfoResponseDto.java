package com.example.naejango.domain.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageInfoResponseDto {
    private String name;
    private String imgUrl;
    private String description;
    private double latitude;
    private double longitude;
    private String address;
}
