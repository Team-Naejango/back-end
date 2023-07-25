package com.example.naejango.domain.follow.dto.response;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindFollowResponseDto {
    private Long id;

    private String name;

    private String description;

    private String imgUrl;

    public FindFollowResponseDto(Storage storage) {
        this.id = storage.getId();
        this.name = storage.getName();
        this.description = storage.getDescription();
        this.imgUrl = storage.getImgUrl();
    }
}
