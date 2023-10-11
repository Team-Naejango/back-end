package com.example.naejango.domain.notification.dto;

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
public class NotificationPublishDto {
    private User receiver;
    private NotificationType notificationType;
    private String content;
    private String url;
}
