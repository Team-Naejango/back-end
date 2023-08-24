package com.example.naejango.domain.chat.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SendMessageRequestDto {
    private Long senderId;
    private String content;
}
