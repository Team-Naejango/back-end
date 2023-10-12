package com.example.naejango.domain.item.dto;

import lombok.*;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class MatchItemsRequestDto {
    @DecimalMax(value = "5000", message = "요청 범위가 너무 큽니다. (0 ~ 5000)")
    @DecimalMin(value = "1000", message = "요청 범위가 너무 작습니다. (0 ~ 5000)")
    private Integer rad;

    @DecimalMax(value = "10", message = "요청 결과물의 개수가 너무 큽니다. (1 ~ 10)")
    @DecimalMin(value = "1", message = "요청 결과물의 개수가 1개 이상이어야 합니다.")
    private Integer size;

    private Long itemId;

    public MatchItemsRequestDto() {
        this.rad = 1000;
        this.size = 5;
    }
}
