package com.example.naejango.domain.storage.dto.request;

import com.example.naejango.domain.storage.dto.Coord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStorageRequestDto {
    @Size(min = 2, max = 25)
    private String name;

    @NotNull
    @Valid
    private Coord coord;

    @NotNull
    @Size(max = 100)
    private String address;

    @NotNull
    @Size(max = 1000)
    private String description;

    @NotNull
    @Size(max = 100)
    private String imgUrl;
}