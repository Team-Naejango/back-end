package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MyStorageListResponseDto {
    private final List<StorageInfo> storageList;
    private final int count;

    public MyStorageListResponseDto(List<Storage> storages) {
        this.storageList = storages.stream().map(StorageInfo::new).collect(Collectors.toList());
        this.count = storages.size();
    }
}