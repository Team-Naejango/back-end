package com.example.naejango.domain.item.dto;

import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.SearchItemRequestDto;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SearchingCommandDto {

    private Point location;
    private int rad;
    private int page;
    private int size;
    private Integer categoryId; // 카테고리
    private String[] keyword; // 검색 키워드
    private ItemType itemType; // 타입 (INDIVIDUAL_BUY/ INDIVIDUAL_SELL/ GROUP_BUY)
    private Boolean status; // 상태 (거래중 / 거래완료)

    public SearchingCommandDto(SearchItemRequestDto searchItemRequestDto, Point location, String[] keywords) {
        this.location = location;
        this.rad = searchItemRequestDto.getRad();
        this.page = searchItemRequestDto.getPage();
        this.size = searchItemRequestDto.getSize();
        this.categoryId = searchItemRequestDto.getCategoryId() != null ? searchItemRequestDto.getCategoryId() : null;
        this.keyword = keywords;
        this.itemType = searchItemRequestDto.getItemType() != null ? searchItemRequestDto.getItemType() : null;
        this.status = searchItemRequestDto.getStatus() != null ? searchItemRequestDto.getStatus() : null;
    }
}
