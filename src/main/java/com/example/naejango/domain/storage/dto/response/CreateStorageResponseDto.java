package com.example.naejango.domain.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateStorageResponseDto {
    private Long id;
    private String name;
    private String imgUrl;
    private String description;
    private double latitude;
    private double longitude;
    private String address;
}