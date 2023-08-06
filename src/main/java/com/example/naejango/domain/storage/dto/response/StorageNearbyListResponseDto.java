package com.example.naejango.domain.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StorageNearbyListResponseDto {
    private List<StorageNearbyInfo> content;
    private int page;
    private int size;
    private int totalCount;
    private int totalPage;
}
