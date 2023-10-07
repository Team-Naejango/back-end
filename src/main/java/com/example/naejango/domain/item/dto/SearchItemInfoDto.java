package com.example.naejango.domain.item.dto;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchItemInfoDto {
    // 창고 정보
    private Long storageId;
    private String storageName;
    private Coord coord;
    private int distance;

    // 아이템 정보
    private Long id;
    private String name;
    private String description;
    private String imgUrl;
    private ItemType itemType;

    // 카테고리
    private int categoryId;
    private String categoryName;

    public SearchItemInfoDto(Item item, Storage storage, Category category, int distance) {
        // 창고 정보
        this.storageId = storage.getId();
        this.storageName = storage.getName();
        this.coord = new Coord(storage.getLocation());
        this.distance = distance;

        // 아이템 정보
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.imgUrl = item.getImgUrl();
        this.itemType = item.getItemType();

        // 카테고리
        this.categoryId = category.getId();
        this.categoryName = category.getName();
    }
}
