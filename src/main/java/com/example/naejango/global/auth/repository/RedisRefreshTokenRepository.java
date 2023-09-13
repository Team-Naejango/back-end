package com.example.naejango.global.auth.repository;

import com.example.naejango.global.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@ConditionalOnProperty(name = "redis-config.refresh-token", havingValue = "true")
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate rt;
    private Duration expire = Duration.ofDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME);

    @Override
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = getRefreshTokenKey(userId);
        rt.opsForValue().set(key, refreshToken, expire);
    }

    @Override
    public String getRefreshToken(Long userId) {
        return rt.opsForValue().get(getRefreshTokenKey(userId));
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        rt.delete(getRefreshTokenKey(userId));
    }

    public String getRefreshTokenKey(Long userId) {
        return JwtProperties.REFRESH_TOKEN_COOKIE_NAME + ":" + userId;
    }

}
