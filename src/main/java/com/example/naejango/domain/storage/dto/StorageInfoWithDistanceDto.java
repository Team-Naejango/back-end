package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class StorageInfoWithDistanceDto {
    private Long storageId;
    private Long ownerId;
    private String name;
    private String imgUrl;
    private String description;
    private String address;
    private Coord coord;
    private int distance;
    public StorageInfoWithDistanceDto(Storage storage, double distance) {
        this.storageId = storage.getId();
        this.ownerId = storage.getUser().getId();
        this.name = storage.getName();
        this.imgUrl = storage.getImgUrl();
        this.description = storage.getDescription();
        this.address = storage.getAddress();
        this.coord = new Coord(storage.getLocation().getX(), storage.getLocation().getY());
        this.distance = (int) Math.round(distance);
    }
}