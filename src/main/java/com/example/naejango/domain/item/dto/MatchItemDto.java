package com.example.naejango.domain.item.dto;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.dto.response.MatchResponseDto;
import com.example.naejango.domain.user.domain.User;
import lombok.*;

import java.util.Arrays;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MatchItemDto {
    private Item item;
    private Category category;
    private User user;
    private Integer distance;

    public MatchResponseDto toResponseDto() {
        return MatchResponseDto.builder()
                .itemId(item.getId())
                .name(item.getName())
                .categoryId(category.getId())
                .category(category.getName())
                .tag(Arrays.asList(item.getTag().split(" ")))
                .imgUrl(item.getImgUrl())
                .itemType(item.getItemType())
                .distance(distance)
                .ownerId(user.getId())
                .build();
    }
}
