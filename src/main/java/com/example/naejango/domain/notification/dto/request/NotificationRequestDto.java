package com.example.naejango.domain.notification.dto.request;

import com.example.naejango.domain.notification.domain.Notification;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    private User receiver;
    private NotificationType notificationType;
    private String content;
    private String url;

    public NotificationRequestDto(Notification notification) {
        this.receiver = notification.getReceiver();
        this.notificationType = notification.getNotificationType();
        this.content = notification.getContent();
        this.url = notification.getUrl();
    }
}
