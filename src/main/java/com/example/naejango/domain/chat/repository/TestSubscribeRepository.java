package com.example.naejango.domain.chat.repository;

import com.example.naejango.global.common.exception.TestException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


// Transaction 테스트를 위한 테스트 stub 입니다.
@Repository
@Profile("TestSubscribeRepository")
@Primary
public class TestSubscribeRepository extends RedisSubscribeRepository {

    public TestSubscribeRepository(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public void saveSubscriptionIdBySessionId(String subscriptionId, String sessionId) {
        if(sessionId.equals("subscribe")) throw new TestException();
        super.saveSubscriptionIdBySessionId(subscriptionId, sessionId);
    }

    @Override
    public void deleteSubscriptionIdBySessionId(String subscriptionId, String sessionId) {
        if(sessionId.equals("unsubscribe")) throw new TestException();
        super.deleteSubscriptionIdBySessionId(subscriptionId, sessionId);
    }

    @Override
    public void deleteSessionId(String sessionId) {
        if(sessionId.equals("disconnect")) throw new TestException();
        super.deleteSessionId(sessionId);
    }
}
