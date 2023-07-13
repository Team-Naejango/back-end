package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.Category;
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
public class ModifyItemRequestDto {
    private String name;

    private String description;

    private String imgUrl;

    private ItemType type;

    private String category;

    public void toEntity(Item item, Category category) {
        item.modifyItem(name, description, imgUrl, type, category);
    }

}
