package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageNearbyListResponseDto {
    private String message;
    private int page;
    private int size;
    private List<StorageNearbyInfoDto> result;

    public StorageNearbyListResponseDto(int page, int size, List<StorageNearbyInfoDto> result) {
        this.message = result.size() == 0? "근처에 창고가 없습니다." : result.size() + "건 조회 되었습니다.";
        this.page = page;
        this.size = size;
        this.result = result;
    }
}
