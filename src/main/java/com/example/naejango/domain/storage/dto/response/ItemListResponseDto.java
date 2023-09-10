package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.storage.dto.ItemInfoDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ItemListResponseDto {
    private String message;
    private int page;
    private int size;
    private List<ItemInfoDto> result;
}
