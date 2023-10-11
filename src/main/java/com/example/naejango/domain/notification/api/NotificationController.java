package com.example.naejango.domain.notification.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.notification.application.NotificationService;
import com.example.naejango.domain.notification.dto.response.NotificationResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final AuthenticationHandler authenticationHandler;
    private final NotificationService notificationService;

    /** 알림 구독 요청 */
    @GetMapping(value = "/api/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication, @RequestHeader(value="Last-Event-ID", required = false, defaultValue = "") String lastEventId){
        Long userId = authenticationHandler.getUserId(authentication);

        return notificationService.subscribe(userId, lastEventId);
    }

    /** 알림 목록 조회 */
    @GetMapping("/api/notification")
    public ResponseEntity<CommonResponseDto<List<NotificationResponseDto>>> findNotification(Authentication authentication){
        Long userId = authenticationHandler.getUserId(authentication);
        List<NotificationResponseDto> list = notificationService.findNotification(userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", list));
    }

    /** 알림 확인 */
    @GetMapping("/api/notification/{id}")
    public ResponseEntity<CommonResponseDto<Void>> checkNotification(Authentication authentication, @PathVariable Long id){
        notificationService.checkNotification(id);

        return ResponseEntity.ok().body(new CommonResponseDto<>("알림 확인 성공", null));
    }
}
