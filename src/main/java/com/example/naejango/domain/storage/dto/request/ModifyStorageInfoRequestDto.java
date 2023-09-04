package com.example.naejango.domain.storage.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModifyStorageInfoRequestDto {
    @Length(min = 2, max = 25)
    private String name;

    @NotNull
    @Length(max = 100)
    private String imgUrl;

    @NotNull
    @Length(max = 1000)
    private String description;
}
