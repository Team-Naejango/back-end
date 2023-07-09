package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StorageListResponseDto {
    private List<StorageInfo> storageList;
    private int count;

    public StorageListResponseDto(List<Storage> storages) {
        this.storageList = storages.stream().map(s -> new StorageInfo(s)).collect(Collectors.toList());
        this.count = storages.size();
    }

    class StorageInfo {
        private Long id;
        private String name;
        private String imgUrl;
        private String address;
        StorageInfo(Storage storage) {
            this.id = storage.getId();
            this.name = storage.getName();
            this.imgUrl = storage.getImgUrl();
            this.address = storage.getAddress();
        }
    }
}
