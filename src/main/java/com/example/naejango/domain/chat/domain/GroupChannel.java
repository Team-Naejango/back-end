package com.example.naejango.domain.chat.domain;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@DiscriminatorValue("GROUP")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class GroupChannel extends Channel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner; // 방 제목, 정원 바꿀 수 있는 방장 기능 추가 예정

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    private int participantsCount;
    private String defaultTitle;
    private int channelLimit;
    public void increaseParticipantCount() { participantsCount++; }
    public void decreaseParticipantCount() { participantsCount--; }
}
