package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemCommandDto {
    private String name;

    private String description;

    private String imgUrl;

    private ItemType itemType;

    private String category;

    private Long storageId;

    public CreateItemCommandDto(CreateItemRequestDto createItemRequestDto) {
        this.name = createItemRequestDto.getName();
        this.description = createItemRequestDto.getDescription();
        this.imgUrl = createItemRequestDto.getImgUrl();
        this.itemType = createItemRequestDto.getItemType();
        this.category = createItemRequestDto.getCategory();
        this.storageId = createItemRequestDto.getStorageId();
    }

    public Item toEntity(User user, Storage storage, Category category) {
        return Item.builder()
                .id(null)
                .name(name)
                .description(description)
                .imgUrl(imgUrl)
                .itemType(itemType)
                .viewCount(0)
                .status(true)
                .user(user)
                .storage(storage)
                .category(category)
                .build();
    }
    
}
