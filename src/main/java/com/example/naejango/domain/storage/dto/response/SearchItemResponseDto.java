package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.storage.dto.Coord;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchItemResponseDto {
    private String message;
    private Coord coord;
    private int radius;
    private int page;
    private int size;
    private List<SearchItemInfoDto> result;

    public SearchItemResponseDto(Coord coord, int radius, int page, int size, List<SearchItemInfoDto> result) {
        this.message = result.isEmpty() ? "검색 결과가 없습니다." : result.size() + "건 조회 되었습니다.";
        this.coord = coord;
        this.radius = radius;
        this.page = page;
        this.size = size;
        this.result = result;
    }
}
