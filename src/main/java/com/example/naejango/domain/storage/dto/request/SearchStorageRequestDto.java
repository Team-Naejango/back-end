package com.example.naejango.domain.storage.dto.request;

import lombok.*;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class SearchStorageRequestDto {
    @DecimalMin(value = "-180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @DecimalMax(value = "180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @NotNull
    private double lon;

    @DecimalMin(value = "-180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @DecimalMax(value = "180.0", message = "올바른 경위도 값을 입력하세요 (-180.0 ~ 180.0)")
    @NotNull
    private double lat;

    @DecimalMax(value = "5000", message = "요청 범위가 너무 큽니다. (0 ~ 5000)")
    @DecimalMin(value = "1000", message = "요청 범위가 너무 작습니다. (0 ~ 5000)")
    private Integer rad;

    @DecimalMin(value = "0", message = "0 이상의 값을 입력해주세요")
    private Integer page;

    @DecimalMax(value = "30", message = "요청 결과물의 개수가 너무 큽니다. (1 ~ 20)")
    @DecimalMin(value = "1", message = "요청 결과물의 개수가 1개 이상이어야 합니다.")
    private Integer size;

    public SearchStorageRequestDto() {
        this.rad = 1000;
        this.page = 0;
        this.size = 20;
    }
}
