package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageNearbyListResponseDto {
    private int page;
    private int size;
    private int totalCount;
    private int totalPage;
    private List<StorageNearbyInfoDto> content;

    public StorageNearbyListResponseDto(int page, int size, int totalCount, List<StorageNearbyInfoDto> content) {
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
        this.totalPage = (totalCount - 1) / size + 1;
        this.content = content;
    }
}
