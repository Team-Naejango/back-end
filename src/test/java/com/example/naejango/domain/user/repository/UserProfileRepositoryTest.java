package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.Optional;

@DataJpaTest
@Transactional
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UserProfile testUserProfile;

    @Test
    @Order(1)
    @DisplayName("Set up: Test UserProfile 생성")
    void setup() {
        testUserProfile = UserProfile.builder()
                .nickname("NICKNAME")
                .imgUrl("IMGURL")
                .phoneNumber("010-0000-0000")
                .age(20)
                .gender(Gender.Male)
                .intro("INTRO")
                .build();
    }

    @Test
    @Order(2)
    @DisplayName("Test1: 저장 및 조회")
    void saveUserProfileTest() {
        // when
        userProfileRepository.save(testUserProfile);

        // then
        Optional<UserProfile> findUserProfile = userProfileRepository.findById(1L);

        Assertions.assertTrue(findUserProfile.isPresent());
        Assertions.assertEquals(findUserProfile.get().getNickname(), testUserProfile.getNickname());
        Assertions.assertNotNull(findUserProfile.get().getCreatedAt());
    }

    @Test
    @Order(3)
    @DisplayName("Test2: Last Login")
    void lastLoginTest() {
        // when
        userProfileRepository.save(testUserProfile);
        UserProfile findUserProfile = userProfileRepository.findById(2L).get();
        // DB는 test 마다 rollback 되어지나 DB내의 id 생성 시퀀스는 초기화 되지 않아 2L 로 저장된다...
        findUserProfile.setLastLogin();

        // then
        Assertions.assertNotNull(findUserProfile.getLastLogin());
        Assertions.assertNotEquals(findUserProfile.getLastLogin(), findUserProfile.getCreatedAt());
    }

}