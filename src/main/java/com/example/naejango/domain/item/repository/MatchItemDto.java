package com.example.naejango.domain.item.repository;

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
    private int distance;
    private User user;

    public MatchResponseDto toResponseDto() {
        return MatchResponseDto.builder()
                .itemId(item.getId())
                .name(item.getName())
                .category(category.getName())
                .tag(Arrays.asList(item.getTag().split(" ")))
                .imgUrl(item.getImgUrl())
                .itemType(item.getItemType())
                .distance(distance)
                .ownerId(user.getId())
                .build();
    }
}
