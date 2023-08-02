package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StorageNearbyDto {
    private Long id;
    private String name;
    private String imgUrl;
    private String description;
    private String address;
    private Coord coord;
    private int distance;
    public StorageNearbyDto(Storage storage, double distance) {
        id = storage.getId();
        name = storage.getName();
        imgUrl = storage.getImgUrl();
        description = storage.getDescription();
        address = storage.getAddress();
        coord = new Coord(storage.getLocation().getX(), storage.getLocation().getY());
        this.distance = (int) Math.round(distance);
    }
}