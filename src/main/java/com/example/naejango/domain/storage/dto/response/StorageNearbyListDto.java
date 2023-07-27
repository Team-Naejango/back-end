package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StorageNearbyListDto {
    private Long id;
    private String name;
    private Point location;
    private int distance;
    public StorageNearbyListDto(Storage storage, int distance) {
        id  = storage.getId();
        name = storage.getName();
        location = storage.getLocation();
        this.distance = distance;
    }
}
