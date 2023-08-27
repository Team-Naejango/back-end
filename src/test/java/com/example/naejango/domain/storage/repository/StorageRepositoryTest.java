package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.handler.GeomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StorageRepositoryTest {
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private UserRepository userRepository;
    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
    private final GeomUtil geomUtil = new GeomUtil();

    @Test
    @DisplayName("Storage 생성 및 조회")
    public void saveAndFindStorage() {
        // given
        User testUser = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .signature("null")
                .build();

        Storage testStorage = Storage.builder()
                .name("test")
                .address("Address")
                .location(factory.createPoint(new Coordinate(12.12, 34.34)))
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
    @DisplayName("userId 로 Storage 조회")
    void findByUserIdTest(){
        // given
        User testUser = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .signature("null")
                .build();

        Storage testStorage1 = Storage.builder()
                .name("1")
                .address("Address")
                .location(factory.createPoint(new Coordinate(12.12, 34.34)))
                .user(testUser)
                .build();

        Storage testStorage2 = Storage.builder()
                .name("2")
                .address("Address")
                .location(factory.createPoint(new Coordinate(56.56, 78.78)))
                .user(testUser)
                .build();

        Storage testStorage3 = Storage.builder()
                .name("3")
                .address("Address")
                .location(factory.createPoint(new Coordinate(-12.12, -34.34)))
                .user(testUser)
                .build();
        userRepository.save(testUser);
        storageRepository.save(testStorage1);
        storageRepository.save(testStorage2);
        storageRepository.save(testStorage3);

        // when
        List<Storage> result = storageRepository.findByUserId(testUser.getId());

        // then
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("좌표 및 반경 기준으로 근처 Storage 조회")
    public void findStorageWithinRadiusTest() {
        // given
        Storage testStorage1 = Storage.builder()
                .name("남산타워").address("")
                .location(factory.createPoint(new Coordinate(126.98820, 37.55126)))
                .build();

        Storage testStorage2 = Storage.builder()
                .name("경복궁").address("")
                .location(factory.createPoint(new Coordinate(126.97689, 37.57760)))
                .build();

        Storage testStorage3 = Storage.builder()
                .name("광화문 이순신 장군").address("")
                .location(factory.createPoint(new Coordinate(126.97700, 37.57098)))
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
        List<Storage> result1 = storageRepository.findStorageWithinRadius(testStorage2.getLocation(), 2000);
        int count1 = storageRepository.countStorageWithinRadius(testStorage2.getLocation(), 2000);
        List<Storage> result2 = storageRepository.findStorageWithinRadius(testStorage1.getLocation(), 2500);
        int count2 = storageRepository.countStorageWithinRadius(testStorage1.getLocation(), 2500);

        // then
        assertThat(result1).containsExactly(testStorage2, testStorage3);
        assertEquals(count1, 2);
        assertThat(result2).containsExactly(testStorage1, testStorage3);
        assertEquals(count2, 2);
    }

    @Test
    @DisplayName("좌표 및 반경 기준으로 근처 Storage 조회 (정렬 및 페이징)")
    void findStorageNearbyTest(){
        // given
        Point center = factory.createPoint(new Coordinate(126.0, 37.0));
        for (int i=1;i<=100;i++){
            double longitude = 126.0 + 0.00001 * i;
            double latitude = 37.0 + 0.00001 * i;
            Point location = factory.createPoint(new Coordinate(longitude, latitude));
            int distance = geomUtil.calculateDistance(center, location);
            Storage testStorage = Storage.builder()
                    .name("Test" + i).address(String.valueOf(distance))
                    .location(location).build();
            storageRepository.save(testStorage);
        }

        // when
        List<StorageNearbyInfoDto> storageNearbyInfo = storageRepository.findStorageNearby(center, 2000, 1, 10);

        // then
        assertThat(storageNearbyInfo.size()).isEqualTo(10);
        assertThat(storageNearbyInfo.get(0).getName()).isEqualTo("Test11");
        assertThat(storageNearbyInfo.get(0).getCoord()).isEqualTo(new Coord(126.0 + 0.00001*11, 37.0 + 0.00001*11));
    }

}