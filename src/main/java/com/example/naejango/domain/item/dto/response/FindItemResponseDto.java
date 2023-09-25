package com.example.naejango.domain.item.dto.response;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindItemResponseDto {
    private Long itemId;
    private Long storageId;
    private Integer categoryId;
    private String categoryName;
    private String itemName;
    private String description;
    private String imgUrl;
    private ItemType itemType;
    private List<String> hashTag;
    private int viewCount;

    public FindItemResponseDto(Item item) {
        this.itemId = item.getId();
        this.storageId = item.getStorage().getId();
        this.categoryId = item.getCategory().getId();
        this.categoryName = item.getCategory().getName();
        this.itemName = item.getName();
        this.description = item.getDescription();
        this.imgUrl = item.getImgUrl();
        this.itemType = item.getItemType();
        this.hashTag = Arrays.asList(item.getTag().split(" "));
        this.viewCount = item.getViewCount();
    }
}
