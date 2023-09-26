package com.example.naejango.domain.item.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyItemRequestDto {
    @NotBlank
    @Size(max = 25)
    private String name;

    @NotNull
    @Size(max = 1000)
    private String description;

    @NotNull
    @Size(max = 100)
    private String imgUrl;

    private Integer category;

}
