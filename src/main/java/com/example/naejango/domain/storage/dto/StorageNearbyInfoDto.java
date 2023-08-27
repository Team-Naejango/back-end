package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class StorageNearbyInfoDto {
    private Long id;
    private String name;
    private String imgUrl;
    private String description;
    private String address;
    private Coord coord;
    private int distance;
    public StorageNearbyInfoDto(Storage storage, double distance) {
        id = storage.getId();
        name = storage.getName();
        imgUrl = storage.getImgUrl();
        description = storage.getDescription();
        address = storage.getAddress();
        coord = new Coord(storage.getLocation().getX(), storage.getLocation().getY());
        this.distance = (int) Math.round(distance);
    }
}