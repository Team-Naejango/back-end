package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StorageInfoDto {
    private Long id;
    private String name;
    private String imgUrl;
    private String description;
    private String address;

    public StorageInfoDto(Storage storage) {
        this.id = storage.getId();
        this.name = storage.getName();
        this.imgUrl = storage.getImgUrl();
        this.description = storage.getDescription();
        this.address = storage.getAddress();
    }
}
