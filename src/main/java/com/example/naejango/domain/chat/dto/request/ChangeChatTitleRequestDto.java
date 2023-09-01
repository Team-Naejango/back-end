package com.example.naejango.domain.chat.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChangeChatTitleRequestDto {
    private String title;
}
