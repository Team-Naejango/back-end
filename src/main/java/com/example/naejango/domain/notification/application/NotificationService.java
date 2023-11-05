package com.example.naejango.domain.notification.application;

import com.example.naejango.domain.notification.domain.Notification;
import com.example.naejango.domain.notification.domain.NotificationType;
import com.example.naejango.domain.notification.dto.response.NotificationResponseDto;
import com.example.naejango.domain.notification.repository.EmitterRepository;
import com.example.naejango.domain.notification.repository.NotificationRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final EntityManager em;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

    /** 알림 구독 요청 */
    @Transactional
    public SseEmitter subscribe(Long userId, String lastEventId) {
        String emitterId = userId + "_" + System.currentTimeMillis();

        // SSE 연결을 위해서 SseEmitter 객체를 만들어 반환
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // Emitter가 완료될 때 Emitter 삭제하도록 설정
        // 비동기 요청이 완료될 때 호출할 코드를 등록
        // 이 메서드는 시간 초과 및 네트워크 오류를 포함한 어떤 이유로든 비동기 요청이 완료되면 컨테이너 스레드에서 호출됨.
        // 이 방법은 인스턴스를 더 이상 사용할 수 없음을 감지하는 데 유용
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        // Emitter가 타임아웃 되었을 때 Emitter 삭제하도록 설정
        // 비동기 요청 시간이 초과될 때 호출할 코드를 등록
        // 이 메서드는 비동기 요청 시간이 초과되면 컨테이너 스레드에서 호출됨
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 503에러 방지를 위해 최초 연결 시 더미 데이터 전송
        sendNotification(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (!lastEventId.isEmpty()) {
            sendLostData(lastEventId, userId, emitter);
        }

        return emitter;
    }

    /** 알림 전송 메서드 */
    @Transactional
    public void send(Long receiverId, NotificationType notificationType, String content, String url) {
        Notification notification = Notification.builder()
                .receiver(em.getReference(User.class, receiverId))
                .notificationType(notificationType)
                .content(content)
                .url(url)
                .build();
        Notification savedNotification = notificationRepository.save(notification);

        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(String.valueOf(receiverId));

        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, savedNotification);
                    sendNotification(emitter, key, new NotificationResponseDto(savedNotification));
                }
        );
    }

    /** 알림 목록 조회 */
    public List<NotificationResponseDto> findNotification(Long userId){
        List<Notification> notificationList = notificationRepository.findByUserId(userId);

        return notificationList.stream().map(NotificationResponseDto::new).collect(Collectors.toList());
    }

    /** 알림 확인 */
    @Transactional
    public void checkNotification(Long userId, Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 알림을 수신한 유저가 맞는지 체크
        if (!notification.getReceiver().getId().equals(userId)){
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.checkNotification();
    }

    private void sendNotification(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name("sse")
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    private void sendLostData(String lastEventId, Long userId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), entry.getValue()));
    }

}
