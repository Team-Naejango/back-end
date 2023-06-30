package com.example.naejango.domain.storage.dto.request;

import lombok.Getter;

@Getter
public class CreateStorageRequestDto {
    private String name;
    private String imgUrl;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
}