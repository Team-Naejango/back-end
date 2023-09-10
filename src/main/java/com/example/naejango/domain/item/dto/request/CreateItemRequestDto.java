package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
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
public class CreateItemRequestDto {
    private String name;

    private String description;

    private String imgUrl;

    private ItemType type;

    private String category;

    private List<Long> storageIdList;

    public Item toEntity(User user, Category category) {
        return Item.builder()
                .id(null)
                .name(name)
                .description(description)
                .imgUrl(imgUrl)
                .type(type)
                .viewCount(0)
                .status(true)
                .user(user)
                .category(category)
                .build();
    }

}
