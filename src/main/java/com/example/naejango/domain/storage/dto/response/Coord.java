package com.example.naejango.domain.storage.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Coord {
    private double longitude;
    private double latitude;

    public Coord(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
