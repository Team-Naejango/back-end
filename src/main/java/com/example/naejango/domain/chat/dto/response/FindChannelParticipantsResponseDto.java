package com.example.naejango.domain.chat.dto.response;

import com.example.naejango.domain.chat.dto.ParticipantInfoDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FindChannelParticipantsResponseDto {
    private int total;
    private List<ParticipantInfoDto> participants;
}
