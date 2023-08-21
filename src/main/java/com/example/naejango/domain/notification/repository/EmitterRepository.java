package com.example.naejango.domain.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    /** Emitter 저장 */
    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    /** 이벤트 저장 */
    void saveEventCache(String emitterId, Object event);

    /** 해당 유저와 관련된 모든 Emitter 조회.
    브라우저당 여러 개 연결이 가능하기에 여러 Emitter 존재 가능 */
    Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId);

    /** 해당 유저와 관련된 모든 이벤트 조회 */
    Map<String, Object> findAllEventCacheStartWithByUserId(String userId);

    /** Emitter 삭제 */
    void deleteById(String id);

    /** 해당 유저와 관련된 모든 Emitter 삭제 */
    void deleteAllEmitterStartWithId(String userId);

    /** 해당 유저와 관련된 모든 이벤트 삭제 */
    void deleteAllEventCacheStartWithId(String userId);
}
