package com.example.naejango.domain.chat.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JoinGroupChannelDto {
    private boolean isCreated;
    private Long chatId;
}
