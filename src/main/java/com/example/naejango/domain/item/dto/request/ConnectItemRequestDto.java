package com.example.naejango.domain.item.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConnectItemRequestDto {
    private Long id;

    private List<Long> storageIdList;
}
