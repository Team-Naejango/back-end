package com.example.naejango.domain.notification.handler;

import com.example.naejango.domain.notification.application.NotificationService;
import com.example.naejango.domain.notification.dto.request.NotificationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener
    @Async
    public void handleNotification(NotificationRequestDto requestDto) {
        notificationService.send(requestDto.getReceiver(), requestDto.getNotificationType(),
                requestDto.getContent(), requestDto.getUrl());
    }
}