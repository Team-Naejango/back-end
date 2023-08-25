package com.example.naejango.domain.item.dto.response;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ItemInfoDto {
    private Long itemId;
    private Category category;
    private String name;
    private String imgUrl;

    public ItemInfoDto(Item item) {
        this.itemId = item.getId();
        this.category = item.getCategory();
        this.name = item.getName();
        this.imgUrl = item.getImgUrl();
    }
}
