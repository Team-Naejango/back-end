package com.example.naejango.domain.item.dto;

import com.example.naejango.domain.item.domain.Category;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CategoryDto {
    private Integer categoryId;
    private String categoryName;

    public CategoryDto(Category category) {
        this.categoryId = category.getId();
        this.categoryName = category.getName();
    }
}
