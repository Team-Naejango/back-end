package com.example.naejango.domain.chat.dto.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipantInfoDto {
    private Long userId;
    private String nickname;
    private String imgUrl;
}
