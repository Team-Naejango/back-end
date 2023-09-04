package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchStorageResultDto {
    private Long storageId; // 창고 ID
    private String name; // 창고 이름
    private String imgUrl; // 창고 이미지 URL
    private String address; // 창고 주소
    private Coord coord; // 창고 좌표
    private int distance; // 요청 좌표와 창고 좌표 사이의 거리

    public SearchStorageResultDto(Storage storage, double distance) {
        this.storageId = storage.getId();
        this.name = storage.getName();
        this.imgUrl = storage.getImgUrl();
        this.address = storage.getAddress();
        this.distance = (int) Math.round(distance);
    }

}