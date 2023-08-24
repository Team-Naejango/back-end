package com.example.naejango.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateRoomRequestDto {
    private Long ChatroomId;
    private Long requestUserId;
    private Long otherUserId;
}
