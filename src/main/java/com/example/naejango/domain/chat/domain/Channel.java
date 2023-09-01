package com.example.naejango.domain.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {
    @Id @GeneratedValue
    @Column(name = "channel_id")
    private Long id;
    private Long ownerId; // 방 제목, 정원 바꿀 수 있는 방장 기능 추가 예정
    private ChatType type;
    private String defaultTitle;
    private int channelLimit; // 방 정원 : 기능 추가 예정
}

