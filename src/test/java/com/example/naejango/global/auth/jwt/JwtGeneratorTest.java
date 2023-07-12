package com.example.naejango.global.auth.jwt;

import com.example.naejango.domain.user.domain.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("jwt")
@SpringBootTest
class JwtGeneratorTest {

    @Autowired
    private JwtGenerator jwtGenerator;

    @Test
    void generateJWT(){
        // given
        User testUser = User.builder().id(1L).userKey("test").build();

        // when
        String accessToken = jwtGenerator.generateAccessToken(testUser.getId());
        String refreshToken = jwtGenerator.generateRefreshToken(testUser.getId());

        // then
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
    }
}