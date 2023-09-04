package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SearchingConditionDto {
    private Category cat; // 카테고리
    private String[] keyword; // 검색 키워드
    private ItemType itemType; // 타입 (BUY / SELL)
    private Boolean status; // 상태 (거래중 / 거래완료)
}
