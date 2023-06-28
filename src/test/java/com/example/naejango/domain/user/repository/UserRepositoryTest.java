package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User 저장 및 조회, ID 컬럼 Generate")
    void test1() {
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
        Optional<User> findUser = userRepository.findByUserKey(testUser.getUserKey());
        Assertions.assertTrue(findUser.isPresent());
        Assertions.assertEquals(findUser.get().getUserKey(), testUser.getUserKey());
        Assertions.assertNotNull(findUser.get().getId());
    }

}