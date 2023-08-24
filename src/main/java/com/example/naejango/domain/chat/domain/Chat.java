package com.example.naejango.domain.chat.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Chat extends TimeAuditingEntity {
    @Id
    @GeneratedValue
    @Column(name = "chatroom_id")
    private Long id;
    private Long ownerId;
    private Long channelId;
    private String title;
    private ChatType type;
    private String lastMessage;

    @OneToMany(mappedBy = "chat")
    private List<ChatMessage> messages;

    public void updateLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
