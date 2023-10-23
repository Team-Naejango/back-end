package com.example.naejango.domain.follow.dto;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FollowStorageItemsDto {
    private Long itemId;
    private String name;
    private String description;
    private String imgUrl;
    private ItemType itemType;
    private Boolean status;

    public FollowStorageItemsDto(Item item) {
        this.itemId = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.imgUrl = item.getImgUrl();
        this.itemType = item.getItemType();
        this.status = item.getStatus();
    }
}