package com.example.naejango.domain.notification.repository;

import com.example.naejango.domain.notification.domain.Notification;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.user.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

class EmitterRepositoryImplTest {
    private EmitterRepository emitterRepository = new EmitterRepositoryImpl();
    private final Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;

    @Test
    @DisplayName("새로운 Emitter 추가")
    public void save() throws Exception {
        //given
        Long userId = 1L;
        String emitterId =  userId + "_" + System.currentTimeMillis();
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        //when, then
        Assertions.assertDoesNotThrow(() -> emitterRepository.save(emitterId, sseEmitter));
    }

    @Test
    @DisplayName("수신한 이벤트를 캐시에 저장")
    public void saveEventCache() throws Exception {
        //given
        Long userId = 1L;
        String emitterId =  userId + "_" + System.currentTimeMillis();

        Notification notification =
                Notification.builder()
                        .content("내용")
                        .url("url")
                        .notificationType(NotificationType.TRANSACTION)
                        .receiver(User.builder().id(userId).build())
                        .build();

        //when, then
        Assertions.assertDoesNotThrow(() -> emitterRepository.saveEventCache(emitterId, notification));
    }

    @Test
    @DisplayName("특정 회원이 접속한 모든 Emitter 조회")
    public void findAllEmitterStartWithByMemberId() throws Exception {
        //given
        Long userId = 1L;
        String emitterId1 =  userId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

        Thread.sleep(100);
        String emitterId2 = userId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

        Thread.sleep(100);
        String emitterId3 = userId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));


        //when
        Map<String, SseEmitter> ActualResult = emitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId));

        //then
        Assertions.assertEquals(3, ActualResult.size());
    }

    @Test
    @DisplayName("특정 회원에게 수신된 이벤트를 캐시에서 모두 조회")
    public void findAllEventCacheStartWithByMemberId() throws Exception {
        //given
        Long userId = 1L;
        String eventCacheId1 =  userId + "_" + System.currentTimeMillis();
        Notification notification1 =
                Notification.builder()
                        .content("내용1")
                        .url("url1")
                        .notificationType(NotificationType.TRANSACTION)
                        .receiver(User.builder().id(userId).build())
                        .build();

        emitterRepository.saveEventCache(eventCacheId1, notification1);

        Thread.sleep(100);
        String eventCacheId2 =  userId + "_" + System.currentTimeMillis();
        Notification notification2 =
                Notification.builder()
                        .content("내용2")
                        .url("url2")
                        .notificationType(NotificationType.TRANSACTION)
                        .receiver(User.builder().id(userId).build())
                        .build();
        emitterRepository.saveEventCache(eventCacheId2, notification2);

        Thread.sleep(100);
        String eventCacheId3 =  userId + "_" + System.currentTimeMillis();
        Notification notification3 =
                Notification.builder()
                        .content("내용3")
                        .url("url3")
                        .notificationType(NotificationType.TRANSACTION)
                        .receiver(User.builder().id(userId).build())
                        .build();
        emitterRepository.saveEventCache(eventCacheId3, notification3);

        //when
        Map<String, Object> ActualResult = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));

        //then
        Assertions.assertEquals(3, ActualResult.size());
    }

    @Test
    @DisplayName("id로 Emitter 삭제")
    public void deleteById() throws Exception {
        //given
        Long userId = 1L;
        String emitterId =  userId + "_" + System.currentTimeMillis();
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        //when
        emitterRepository.save(emitterId, sseEmitter);
        emitterRepository.deleteById(emitterId);

        //then
        Assertions.assertEquals(0, emitterRepository.findAllEmitterStartWithByUserId(emitterId).size());
    }

    @Test
    @DisplayName("저장된 모든 Emitter 삭제")
    public void deleteAllEmitterStartWithId() throws Exception {
        //given
        Long userId = 1L;
        String emitterId1 = userId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

        Thread.sleep(100);
        String emitterId2 = userId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

        //when
        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userId));

        //then
        Assertions.assertEquals(0, emitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId)).size());
    }

    @Test
    @DisplayName("저장된 모든 이벤트 삭제")
    public void deleteAllEventCacheStartWithId() throws Exception {
        //given
        Long userId = 1L;
        String eventCacheId1 =  userId + "_" + System.currentTimeMillis();
        Notification notification1 =
                Notification.builder()
                        .content("내용1")
                        .url("url1")
                        .notificationType(NotificationType.TRANSACTION)
                        .receiver(User.builder().id(userId).build())
                        .build();
        emitterRepository.saveEventCache(eventCacheId1, notification1);

        Thread.sleep(100);
        String eventCacheId2 =  userId + "_" + System.currentTimeMillis();
        Notification notification2 =
                Notification.builder()
                    .content("내용2")
                    .url("url2")
                    .notificationType(NotificationType.TRANSACTION)
                    .receiver(User.builder().id(userId).build())
                    .build();
        emitterRepository.saveEventCache(eventCacheId2, notification2);

        //when
        emitterRepository.deleteAllEventCacheStartWithId(String.valueOf(userId));

        //then
        Assertions.assertEquals(0, emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId)).size());
    }
}