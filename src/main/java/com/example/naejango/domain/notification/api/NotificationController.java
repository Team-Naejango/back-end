package com.example.naejango.domain.notification.api;

import com.example.naejango.domain.notification.application.NotificationService;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequestMapping("/api/subscribe")
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final CommonDtoHandler commonDtoHandler;
    private final NotificationService notificationService;

    /** 알림 구독 요청 */
    @GetMapping(produces = "text/event-stream")
    public SseEmitter subscribe(Authentication authentication, @RequestHeader(value="Last-Event-ID", required = false, defaultValue = "") String lastEventId ){
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);

        return notificationService.subscribe(userId, lastEventId);
    }
}
