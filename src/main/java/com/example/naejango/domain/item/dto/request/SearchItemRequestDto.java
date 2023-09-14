package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.global.common.validation.EnumConstraint;
import lombok.*;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class SearchItemRequestDto {
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

    @DecimalMax(value = "20", message = "요청 결과물의 개수가 너무 큽니다. (1 ~ 20)")
    @DecimalMin(value = "1", message = "요청 결과물의 개수가 1개 이상이어야 합니다.")
    private Integer size;

    private String category; // 카테고리

    @Size(min = 2, max = 10, message = "검색어가 너무 길거나 짧습니다. (2자 ~ 10자)")
    private String keyword; // 검색 키워드

    @EnumConstraint(enumClass = ItemType.class, defaultValue = "null")
    private ItemType itemType; // 타입 (INDIVIDUAL_BUY/ INDIVIDUAL_SELL/ GROUP_BUY)

    private Boolean status; // 상태 (거래중 / 거래완료)

}
