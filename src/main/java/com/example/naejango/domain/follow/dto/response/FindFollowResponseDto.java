package com.example.naejango.domain.follow.dto.response;

import com.example.naejango.domain.follow.dto.FollowStorageItemsDto;
import com.example.naejango.domain.storage.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindFollowResponseDto {
    private Long id;

    private String name;

    private String description;

    private String imgUrl;

    private List<FollowStorageItemsDto> items;

    public FindFollowResponseDto(Storage storage) {
        this.id = storage.getId();
        this.name = storage.getName();
        this.description = storage.getDescription();
        this.imgUrl = storage.getImgUrl();
        this.items = storage.getItems().stream().map(FollowStorageItemsDto::new).collect(Collectors.toList());
    }
}
