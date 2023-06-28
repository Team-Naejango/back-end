package com.example.naejango.domain.user.repository;


import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
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

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class UserUserProfileRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("엔티티 맵핑 테스트: User 객체 저장 -> UserProfile 객체 저장")
    void test1() {
        // given
        User testUser = User.builder()
                .userKey("TEST_1234")
                .role(Role.USER)
                .password("NULL")
                .signature("REFRESH_TOKEN")
                .build();

        UserProfile testUserProfile = UserProfile.builder()
                .nickname("NICKNAME")
                .imgUrl("IMGURL")
                .phoneNumber("010-0000-0000")
                .age(20)
                .gender(Gender.Male)
                .intro("INTRO")
                .build();

        // when
        User saveUser = userRepository.save(testUser);
        UserProfile saveUserProfile = userProfileRepository.save(testUserProfile);
        saveUser.updateProfile(saveUserProfile);

        // then
        User findUser = userRepository.findByUserKey(testUser.getUserKey()).get();
        UserProfile findUserProfile = userProfileRepository.findById(testUserProfile.getId()).get();

        Assertions.assertEquals(findUser.getUserProfile().getId(), findUserProfile.getId());
    }

}