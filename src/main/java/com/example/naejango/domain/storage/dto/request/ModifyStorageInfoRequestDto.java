package com.example.naejango.domain.storage.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModifyStorageInfoRequestDto {
    @Length(min = 2, max = 12)
    private String name;

    @NotNull
    @Length(max = 100)
    private String imgUrl;

    @NotNull
    @Length(max = 100)
    private String description;
}
