package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.StorageAndDistanceDto;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StorageRepositoryTest {
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private UserRepository userRepository;

    private final GeomUtil geomUtil = new GeomUtil();

    @Test
    @DisplayName("Storage 생성 및 조회")
    public void saveAndFindStorage() {
        // given
        User testUser = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .refreshToken("null")
                .build();

        Storage testStorage = Storage.builder()
                .name("test")
                .address("Address")
                .location(geomUtil.createPoint(12.12, 34.34))
                .user(testUser)
                .build();

        storageRepository.save(testStorage);

        // when
        Storage findStorage = storageRepository.findById(testStorage.getId()).orElse(
                Storage.builder().name("FAIL").build()
        );

        // then
        assertEquals(findStorage.getName(), testStorage.getName());
        assertEquals(findStorage.getLocation().getX(), 12.12);
        assertEquals(findStorage.getLocation().getY(), 34.34);
    }

    @Test
    @DisplayName("좌표 및 반경 기준으로 근처 Storage 조회")
    public void findStorageWithinRadiusTest() {
        // given
        Storage testStorage1 = Storage.builder()
                .name("남산타워").address("")
                .location(geomUtil.createPoint(126.98820, 37.55126))
                .build();

        Storage testStorage2 = Storage.builder()
                .name("경복궁").address("")
                .location(geomUtil.createPoint(126.97689, 37.57760))
                .build();

        Storage testStorage3 = Storage.builder()
                .name("광화문 이순신 장군").address("")
                .location(geomUtil.createPoint(126.97700, 37.57098))
                .build();

        int d12 = geomUtil.calculateDistance(testStorage1.getLocation(), testStorage2.getLocation());
        int d23 = geomUtil.calculateDistance(testStorage2.getLocation(), testStorage3.getLocation());
        int d31 = geomUtil.calculateDistance(testStorage1.getLocation(), testStorage3.getLocation());

        log.info("d12(남산타워 ~ 경복궁) = {}", d12); // 3094 m
        log.info("d23(경복궁 ~ 이순신) = {}", d23); // 736 m
        log.info("d31(이순신 ~ 남산타워)= {}", d31); // 2405 m

        storageRepository.save(testStorage1);
        storageRepository.save(testStorage2);
        storageRepository.save(testStorage3);

        // when
        List<StorageAndDistanceDto> result1 = storageRepository.findStorageNearby(testStorage2.getLocation(), 2000, 0, 10);
        List<StorageAndDistanceDto> result2 = storageRepository.findStorageNearby(testStorage1.getLocation(), 3000, 0, 10);

        // then

        assertEquals(result1.size(), 2);
        assertEquals(result2.size(), 2);
    }

    @Test
    @DisplayName("창고 ID로 유저 ID 조회")
    public void findUserIdByStorageId() {
        // given
        User testUser1 = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .refreshToken("null")
                .build();

        User testUser2 = User.builder()
                .userKey("test_5678")
                .password("null")
                .role(Role.USER)
                .refreshToken("null")
                .build();

        Storage testStorage1 = Storage.builder()
                .name("남산타워").address("")
                .location(geomUtil.createPoint(126.98820, 37.55126))
                .user(testUser1)
                .build();

        Storage testStorage2 = Storage.builder()
                .name("경복궁").address("")
                .location(geomUtil.createPoint(126.97689, 37.57760))
                .user(testUser2)
                .build();

        Storage testStorage3 = Storage.builder()
                .name("광화문 이순신 장군").address("")
                .location(geomUtil.createPoint(126.97700, 37.57098))
                .user(testUser2)
                .build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);
        storageRepository.save(testStorage1);
        storageRepository.save(testStorage2);
        storageRepository.save(testStorage3);

        // when
        Long userId1 = storageRepository.findUserIdByStorageId(testStorage1.getId());
        Long userId2 = storageRepository.findUserIdByStorageId(testStorage2.getId());

        // then
        assertEquals(testUser1.getId(), userId1);
        assertEquals(testUser2.getId(), userId2);
    }
}