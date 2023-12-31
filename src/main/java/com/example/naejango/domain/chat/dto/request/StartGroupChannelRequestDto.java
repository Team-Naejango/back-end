package com.example.naejango.domain.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartGroupChannelRequestDto {
    private Long itemId;
    private String defaultTitle;
    private int limit;
}
