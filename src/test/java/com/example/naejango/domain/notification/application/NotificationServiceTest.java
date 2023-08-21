package com.example.naejango.domain.notification.application;

import com.example.naejango.domain.notification.domain.Notification;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.notification.repository.EmitterRepository;
import com.example.naejango.domain.notification.repository.NotificationRepository;
import com.example.naejango.domain.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;
    @Mock
    private EmitterRepository emitterRepository;
    @Mock
    private NotificationRepository notificationRepository;
    private final Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;

    @Test
    @Order(1)
    @DisplayName("알림 구독 요청")
    public void subscribe() throws Exception {
        //given
        User user = User.builder().id(1L).build();
        String lastEventId = "";
        BDDMockito.given(emitterRepository.save(any(), any())).willReturn(new SseEmitter(DEFAULT_TIMEOUT));

        //when, then
        notificationService.subscribe(user.getId(), lastEventId);
        Assertions.assertDoesNotThrow(() -> notificationService.subscribe(user.getId(), lastEventId));

    }

    @Test
    @Order(2)
    @DisplayName("알림 전송")
    public void send() throws Exception {
        //given
        User user = User.builder().id(1L).build();
        String lastEventId = "";
        String emitterId = user.getId() + "_" + System.currentTimeMillis();

        Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
        emitters.put(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        Notification notification = Notification.builder()
                .receiver(user)
                .notificationType(NotificationType.TRANSACTION)
                .content("알림 내용")
                .url("알림 url")
                .build();

        BDDMockito.given(emitterRepository.save(any(), any())).willReturn(new SseEmitter(DEFAULT_TIMEOUT));
        BDDMockito.given(notificationRepository.save(any())).willReturn(notification);
        BDDMockito.given(emitterRepository.findAllEmitterStartWithByUserId(any())).willReturn(emitters);

        //when, then
        notificationService.subscribe(user.getId(), lastEventId);


        Assertions.assertDoesNotThrow(() -> notificationService.send(user, NotificationType.TRANSACTION, "알림 내용", "알림 url"));
        verify(emitterRepository).saveEventCache(any(), any());
    }
}