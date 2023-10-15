package com.example.naejango.domain.storage.dto;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StorageAndDistanceDto {
    private Storage storage;
    private Integer distance;
}