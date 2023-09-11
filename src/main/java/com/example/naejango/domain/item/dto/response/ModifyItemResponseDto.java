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
public class ModifyItemResponseDto {
    private Long id;

    private String name;

    private String description;

    private String imgUrl;

    private ItemType type;

    private String category;

    public ModifyItemResponseDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.imgUrl = item.getImgUrl();
        this.type = item.getItemType();
        this.category = item.getCategory().toString();
    }
}
