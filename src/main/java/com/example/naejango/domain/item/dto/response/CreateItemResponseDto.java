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
public class CreateItemResponseDto {

    private Long id;

    private String category;

    private String name;

    private String description;

    private ItemType type;

    public CreateItemResponseDto(Item item) {
        this.id = item.getId();
        this.category = item.getCategory().toString();
        this.name = item.getName();
        this.description = item.getDescription();
        this.type = item.getType();
    }
}
