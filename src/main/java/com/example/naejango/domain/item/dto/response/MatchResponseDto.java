package com.example.naejango.domain.item.dto.response;

import com.example.naejango.domain.item.domain.ItemType;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MatchResponseDto {
    private Long itemId;
    private int categoryId;
    private String category;
    private String name;
    private String imgUrl;
    private ItemType itemType;
    private int distance;
    private List<String> tag;
    private Long ownerId;
}
