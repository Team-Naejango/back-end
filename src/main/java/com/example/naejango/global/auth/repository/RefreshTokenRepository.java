package com.example.naejango.global.auth.repository;

public interface RefreshTokenRepository {
    void saveRefreshToken(Long userId, String refreshToken);
    String getRefreshToken(Long userId);
    void deleteRefreshToken(Long userId);
}
