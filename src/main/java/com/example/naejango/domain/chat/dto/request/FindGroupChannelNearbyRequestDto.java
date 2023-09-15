package com.example.naejango.domain.chat.dto.request;


import lombok.*;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Getter
@Setter
@ToString
public class FindGroupChannelNearbyRequestDto {
    @DecimalMin(value = "-180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @DecimalMax(value = "180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    private double lon;

    @DecimalMin(value = "-180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @DecimalMax(value = "180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    private double lat;

    @DecimalMax(value = "5000", message = "요청 범위가 너무 큽니다. (0 ~ 5000)")
    @DecimalMin(value = "1000", message = "요청 범위가 너무 작습니다. (0 ~ 5000)")
    private int rad;

    public FindGroupChannelNearbyRequestDto() {
        this.rad = 1000;
    }
}
