package com.example.naejango.domain.chat.dto.response;

import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.storage.dto.Coord;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FindGroupChannelNearbyResponseDto {
    private String message;
    private Coord center;
    private int radius;
    private List<GroupChannelDto> result;
}
