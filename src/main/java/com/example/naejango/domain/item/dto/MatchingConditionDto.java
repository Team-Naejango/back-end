package com.example.naejango.domain.item.dto;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MatchingConditionDto {
    private Category category;
    private ItemType[] itemTypes;
    private String[] hashTags;
}
