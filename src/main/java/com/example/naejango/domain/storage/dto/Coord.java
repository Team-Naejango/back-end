package com.example.naejango.domain.storage.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Coord {

    @DecimalMin(value = "-180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @DecimalMax(value = "180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    private double longitude;

    @DecimalMin(value = "-180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @DecimalMax(value = "180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    private double latitude;

    public Coord(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
