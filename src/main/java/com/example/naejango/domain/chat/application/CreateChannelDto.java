package com.example.naejango.domain.chat.application;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateChannelDto {
    private boolean isCreated;
    private Long channelId;
    private Long chatId;

}
