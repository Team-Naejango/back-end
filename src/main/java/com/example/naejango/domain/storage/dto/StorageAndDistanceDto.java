package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@Builder
@ToString
public class StorageAndDistanceDto {
    private Storage storage;
    private double distance;

    public StorageAndDistanceDto(Storage storage, double distance) {
        this.storage = storage;
        this.distance = distance;
    }
}