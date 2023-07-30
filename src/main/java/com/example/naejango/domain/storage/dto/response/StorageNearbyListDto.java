package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StorageNearbyListDto {
    private Long id;
    private String name;
    private double longitude;
    private double latitude;
    private int distance;

    public StorageNearbyListDto(Storage storage, int distance) {
        this(storage.getId(), storage.getName(), storage.getLocation().getX(), storage.getLocation().getY(), distance);
    }
}
