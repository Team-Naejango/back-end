package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyItemCommandDto {

    private String name;

    private String description;

    private String imgUrl;

    private Integer category;

    public ModifyItemCommandDto(ModifyItemRequestDto modifyItemRequestDto) {
        this.name = modifyItemRequestDto.getName();
        this.description = modifyItemRequestDto.getDescription();
        this.imgUrl = modifyItemRequestDto.getImgUrl();
        this.category = modifyItemRequestDto.getCategory();
    }

    public void toEntity(Item item, Category category) {
        item.modifyItem(name, description, imgUrl, category);
    }

}
