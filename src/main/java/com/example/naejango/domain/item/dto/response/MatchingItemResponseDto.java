package com.example.naejango.domain.item.dto.response;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class MatchingItemResponseDto {
    private Long id; // 창고 ID
    private String name; // 창고 이름
    private String imgUrl; // 창고 이미지 URL
    private String description; // 창고 설명
    private String address; // 창고 주소
    private Coord coord; // 창고 좌표
    private int distance; // 요청 좌표와 창고 좌표 사이의 거리
    private Long userId; // 창고 등록유저의 ID
    private String type; // 조회한 아이템의 타입 BUY, SELL

    public MatchingItemResponseDto(Storage storage, Item item, double distance) {
        this.id = storage.getId();
        this.name = storage.getName();
        this.imgUrl = storage.getImgUrl();
        this.description = storage.getDescription();
        this.address = storage.getAddress();
        this.coord = new Coord(storage.getLocation().getX(), storage.getLocation().getY());
        this.distance = (int) Math.round(distance);
        this.userId = storage.getUser().getId();
        this.type = item.getType().toString();
    }
}