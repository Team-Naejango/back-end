package com.example.naejango.domain.chat.dto;


import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SubscribeResponseDto {

    private Long userId;
    private Long subscribingChannelId;
    private String message;

}
