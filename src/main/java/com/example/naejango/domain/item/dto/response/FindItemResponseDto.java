package com.example.naejango.domain.item.dto.response;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindItemResponseDto {
    private Long id;

    private String name;

    private String description;

    private String imgUrl;

    private ItemType itemType;

    private String category;

    private int viewCount;

    public FindItemResponseDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.imgUrl = item.getImgUrl();
        this.itemType = item.getItemType();
        this.category = item.getCategory().getName();
        this.viewCount = item.getViewCount();
    }
}
