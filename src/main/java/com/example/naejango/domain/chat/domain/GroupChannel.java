package com.example.naejango.domain.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("GROUP")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GroupChannel extends Channel {
    private Long ownerId; // 방 제목, 정원 바꿀 수 있는 방장 기능 추가 예정
    private Long storageId;
    private int participantsCount;
    private String defaultTitle;
    private int channelLimit; // 방 정원 : 기능 추가 예정
    public void join() {
        participantsCount++;
    }
}
