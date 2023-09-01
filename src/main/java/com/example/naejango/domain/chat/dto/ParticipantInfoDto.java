package com.example.naejango.domain.chat.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ParticipantInfoDto {
    private Long participantId;
    private String nickname;
    private String imgUrl;
}
