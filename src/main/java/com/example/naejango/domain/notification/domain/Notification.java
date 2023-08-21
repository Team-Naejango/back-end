package com.example.naejango.domain.notification.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import com.example.naejango.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notification")
public class Notification extends TimeAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 알림 내용

    @Column(nullable = false)
    private String url; // 관련 링크

    @Column(nullable = false)
    private Boolean isRead; // 알림 확인 여부

    @Column(nullable = false)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User receiver; // 알림 받는 사람

    @Builder
    public Notification(String content, String url, NotificationType notificationType, User receiver) {
        this.content = content;
        this.url = url;
        this.notificationType = notificationType;
        this.isRead = false;
        this.receiver = receiver;
    }

}
