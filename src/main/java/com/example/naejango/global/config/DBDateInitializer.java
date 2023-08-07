package com.example.naejango.global.config;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DBDateInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        addGuestUser();
    }

    /**
     * 둘러보기 용 회원 추가 메서드
     */
    private void addGuestUser() {
        User guest = User.builder().userKey("Guest")
                .role(Role.GUEST)
                .password("").build();

        UserProfile guestProfile = UserProfile.builder()
                .nickname("Guest")
                .phoneNumber("01012345678")
                .gender(Gender.Male)
                .birth("20230701")
                .intro("서비스 둘러보기용 회원입니다.")
                .imgUrl("").build();

        userRepository.save(guest);
        userService.createUserProfile(guestProfile, guest.getId());
    }
}
