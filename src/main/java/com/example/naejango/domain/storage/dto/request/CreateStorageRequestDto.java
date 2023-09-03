package com.example.naejango.domain.storage.dto.request;

import com.example.naejango.domain.storage.dto.Coord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStorageRequestDto {
    @Length(min = 2, max = 12)
    private String name;

    @NotNull
    @Valid
    private Coord coord;

    @NotNull
    @Length(max = 100)
    private String address;

    @NotNull
    private String description;

    @NotNull
    @Length(max = 100)
    private String imgUrl;
}