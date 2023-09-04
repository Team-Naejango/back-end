package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.application.SearchingConditionDto;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.SearchStorageResultDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchStorageResponseDto {
    private String message;
    private Coord coord;
    private int radius;
    private int page;
    private int size;
    private SearchingConditionDto searchingConditions;
    private List<SearchStorageResultDto> searchResult;

    public SearchStorageResponseDto(Coord coord, int radius, int page, int size, SearchingConditionDto searchingConditions, List<SearchStorageResultDto> searchResult) {
        this.message = searchResult == null ? "검색 결과가 없습니다." : searchResult.size() + "건 조회 되었습니다.";
        this.coord = coord;
        this.radius = radius;
        this.page = page;
        this.size = size;
        this.searchingConditions = searchingConditions;
        this.searchResult = searchResult;
    }
}
