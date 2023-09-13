package com.example.naejango.domain.storage.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModifyStorageInfoRequestDto {
    @Size(min = 2, max = 25)
    private String name;

    @NotNull
    @Size(max = 100)
    private String imgUrl;

    @NotNull
    @Size(max = 1000)
    private String description;
}
