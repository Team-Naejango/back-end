package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestModifyItem {

    private Long id;

    private Long StorageId;

    private String category;

    private String name;

    private String description;

    private String imgUrl;

    private ItemType type;


    public void toEntity(Item item, Category category) {
        item.modifyItem(name, description, imgUrl, type, category);
    }

}
