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

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemCommandDto {
    private String name;

    private String description;

    private String imgUrl;

    private ItemType itemType;

    private List<String> tag;

    private int categoryId;

    private Long storageId;

    public CreateItemCommandDto(CreateItemRequestDto createItemRequestDto) {
        this.name = createItemRequestDto.getName();
        this.description = createItemRequestDto.getDescription();
        this.imgUrl = createItemRequestDto.getImgUrl();
        this.itemType = createItemRequestDto.getItemType();
        this.tag = createItemRequestDto.getHashTag();
        this.categoryId = createItemRequestDto.getCategoryId();
        this.storageId = createItemRequestDto.getStorageId();
    }

    public Item toEntity(User user, Storage storage, Category category) {
        return Item.builder()
                .id(null)
                .name(name)
                .description(description)
                .imgUrl(imgUrl)
                .itemType(itemType)
                .tag(String.join(" ", tag))
                .viewCount(0)
                .status(true)
                .user(user)
                .storage(storage)
                .category(category)
                .build();
    }

}
