package com.example.naejango.global.auth.repository;

import com.example.naejango.domain.user.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Primary
public interface BasicRefreshTokenRepository extends RefreshTokenRepository, JpaRepository<User, Long> {
    @Override
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :userId")
    void saveRefreshToken(@Param("userId") Long userId, @Param("refreshToken")String refreshToken);

    @Override
    @Query("SELECT u.refreshToken FROM User u WHERE u.id = :userId")
    String getRefreshToken(@Param("userId") Long userId);

    @Override
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = null WHERE u.id = :userId")
    void deleteRefreshToken(@Param("userId") Long userId);
}

