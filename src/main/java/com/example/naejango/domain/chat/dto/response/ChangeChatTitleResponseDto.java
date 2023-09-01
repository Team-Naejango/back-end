package com.example.naejango.domain.chat.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChangeChatTitleResponseDto {
    private Long chatId;
    private String changedTitle;
}
