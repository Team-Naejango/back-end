package com.example.naejango.domain.storage.dto.response;

import com.example.naejango.domain.chat.dto.GroupChannelDto;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FindStorageChannelResponseDto {
    private GroupChannelDto channelInfo;
    private String message;
}
