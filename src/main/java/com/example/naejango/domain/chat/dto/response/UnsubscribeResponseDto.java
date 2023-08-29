package com.example.naejango.domain.chat.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UnsubscribeResponseDto {

    private Long userId;
    private Long unsubscribedChannelId;
    private String message;

}
