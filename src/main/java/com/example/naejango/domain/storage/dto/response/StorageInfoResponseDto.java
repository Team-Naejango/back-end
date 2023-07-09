package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StorageInfoResponseDto {
    private String name;
    private String imgUrl;
    private String description;
    private double latitude;
    private double longitude;
    private String address;

    public StorageInfoResponseDto(Storage storage) {
        this.name = storage.getName();
        this.imgUrl = storage.getImgUrl();
        this.description = storage.getDescription();
        this.latitude = storage.getLocation().getLatitude();
        this.longitude = storage.getLocation().getLongitude();
        this.address = storage.getAddress();
    }
}
