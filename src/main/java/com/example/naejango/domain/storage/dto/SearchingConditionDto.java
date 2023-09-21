package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.SearchItemRequestDto;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SearchingConditionDto {
    private String category; // 카테고리
    private String[] keyword; // 검색 키워드
    private ItemType[] itemType; // 타입 (INDIVIDUAL_BUY/ INDIVIDUAL_SELL/ GROUP_BUY)
    private Boolean status; // 상태 (거래중 / 거래완료)

    public SearchingConditionDto(SearchItemRequestDto searchItemRequestDto, String[] keywords) {
        this.category = searchItemRequestDto.getCategory();
        this.keyword = keywords;
        this.itemType = new ItemType[]{searchItemRequestDto.getItemType()};
        this.status = searchItemRequestDto.getStatus();
    }
}
