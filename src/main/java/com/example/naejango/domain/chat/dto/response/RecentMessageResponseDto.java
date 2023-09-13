package com.example.naejango.domain.chat.dto.response;

import com.example.naejango.domain.chat.dto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentMessageResponseDto {
    private int page;
    private int size;
    private List<MessageDto> messages;
}
