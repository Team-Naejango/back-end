package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ItemInfoDto {
    private Long itemId;
    private String category;
    private ItemType type;
    private String name;
    private String imgUrl;
    private String description;

    public ItemInfoDto(Item item, String category) {
        this.itemId = item.getId();
        this.category = category;
        this.type = item.getType();
        this.name = item.getName();
        this.imgUrl = item.getImgUrl();
        this.description = item.getDescription();
    }
}
