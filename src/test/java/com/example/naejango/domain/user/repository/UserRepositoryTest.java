package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserProfileRepository userProfileRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    @DisplayName("User 저장 및 조회")
    void basicTest() {
        // given
        User testUser = User.builder()
                .userKey("TEST_1234")
                .role(Role.USER)
                .password("NULL")
                .signature("REFRESH_TOKEN")
                .userProfile(null)
                .build();

        // when
        userRepository.save(testUser);

        // then
        Optional<User> findUser = userRepository.findById(testUser.getId());
        assertTrue(findUser.isPresent());
        assertEquals(findUser.get().getUserKey(), testUser.getUserKey());
    }

    @Test
    @Transactional
    @DisplayName("findUserWithProfile : User, UserProfile 저장 후 쿼리 한번에 조회")
    public void findUserWithProfileTest() {
        // given
        User testUser = User.builder()
                .userKey("TEST_1234")
                .role(Role.USER)
                .password("NULL")
                .signature("REFRESH_TOKEN")
                .userProfile(null)
                .build();

        UserProfile testUserProfile = UserProfile.builder()
                .age(20)
                .gender(Gender.Male)
                .phoneNumber("010-0000-0000")
                .nickname("Nick")
                .build();

        userRepository.save(testUser);
        userProfileRepository.save(testUserProfile);
        testUser.createUserProfile(testUserProfile);

        em.flush();
        em.clear();
        
        // when
        Optional<User> userWithProfileByUserId = userRepository.findUserWithProfileById(testUser.getId());
        User user = userWithProfileByUserId.get();

        // then
        assertTrue(userWithProfileByUserId.isPresent());
        // 1회의 쿼리로 User 및 UserProfile 을 조회
        assertEquals(user.getUserProfile().getNickname(), testUserProfile.getNickname());
    }
    
    @Test
    @Transactional
    @DisplayName("User 삭제")
    public void deleteUserTest() {
        // given
        User testUser = User.builder()
                .userKey("TEST_1234")
                .role(Role.USER)
                .password("NULL")
                .signature("REFRESH_TOKEN")
                .userProfile(null)
                .build();
        
        userRepository.save(testUser);
        
        em.flush();
        em.clear();
        
        // when
        User savedUser = userRepository.findByUserKey("TEST_1234").orElseGet(() -> {
            return User.builder().userKey("FAIL").build();
        });

        userRepository.deleteUserById(savedUser.getId());

        em.flush();
        em.clear();

        // then
        User findUser = userRepository.findByUserKey("TEST_1234").orElseGet(() -> {
            return User.builder().userKey("SUCCESS").build();
        });

        assertEquals("SUCCESS", findUser.getUserKey());
    }


}