package com.example.naejango.domain.chat.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Message extends TimeAuditingEntity {
    @Id
    @GeneratedValue
    @Column(name = "message_id")
    private Long id;
    private MessageType messageType;
    private Long senderId;
    private String content;

    @OneToMany(mappedBy = "message")
    private List<ChatMessage> chatMessages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

}
