package com.example.naejango.domain.item.dto.request;

import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.global.common.validation.EnumConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class
CreateItemRequestDto {
    @NotBlank
    @Size(max = 25)
    private String name;

    @NotNull
    @Size(max = 1000)
    private String description;

    @NotNull
    @Size(max = 100)
    private String imgUrl;

    @Size(min = 1, max = 3)
    private List<String> hashTag;

    @EnumConstraint(enumClass = ItemType.class, message = "올바른 Type을 입력하세요. (INDIVIDUAL_BUY/INDIVIDUAL_SELL/GROUP_BUY)")
    private ItemType itemType;

    private int categoryId;

    private Long storageId;

}
