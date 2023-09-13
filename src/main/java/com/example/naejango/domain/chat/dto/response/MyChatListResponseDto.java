package com.example.naejango.domain.chat.dto.response;

import com.example.naejango.domain.chat.dto.ChatInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyChatListResponseDto {
    private String message;
    private int page;
    private int size;
    private List<ChatInfoDto> chatInfoList;
}
