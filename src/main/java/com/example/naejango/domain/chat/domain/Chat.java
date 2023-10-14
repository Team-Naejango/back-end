package com.example.naejango.domain.chat.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import com.example.naejango.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Chat extends TimeAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    private String title;

    @OneToMany(mappedBy = "chat")
    private List<ChatMessage> chatMessages;

    public void changeTitle(String title) {
        this.title = title;
    }
}
