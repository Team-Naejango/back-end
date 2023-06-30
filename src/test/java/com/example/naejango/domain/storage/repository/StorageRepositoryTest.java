package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Location;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StorageRepositoryTest {
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Storage 저장")
    void saveStorage() {
        // given
        Location testLocation = Location.builder()
                .latitude(123.123)
                .longitude(456.456)
                .build();

        User testUser = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .signature("null")
                .build();

        Storage testStorage = Storage.builder()
                .name("Test Storage")
                .imgUrl("Test Url")
                .address("Test Address")
                .description("This is for a test")
                .location(testLocation)
                .user(testUser)
                .build();

        // when
        userRepository.save(testUser);
        Storage saveStorage = storageRepository.save(testStorage);
        Storage findStorage = storageRepository.findById(saveStorage.getId())
                .orElse(Storage.builder().name("fail").build());

        log.info("findStorage = {}",findStorage.toString());

        // then
        Assertions.assertEquals(testStorage.getLocation().getLatitude(), 123.123);
    }

}