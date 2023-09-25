package com.example.naejango.domain.item.dto;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.storage.domain.Storage;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchItemsDto {
    private Item item;
    private Category category;
    private Storage storage;
    private Integer distance;
}
