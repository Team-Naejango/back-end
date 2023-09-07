package com.example.naejango.domain.item.dto.response;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemDealType;
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

    private ItemType type;

    private ItemDealType dealType;

    private String category;

    public FindItemResponseDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.imgUrl = item.getImgUrl();
        this.type = item.getType();
        this.dealType = item.getDealType();
        this.category = item.getCategory().getName();
    }
}
