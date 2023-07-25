package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
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
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("save: 창고 생성")
    public void saveStorage() {
        // given
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
                .location(factory.createPoint(new Coordinate(12.12, 34.34)))
                .user(testUser)
                .build();

        storageRepository.save(testStorage);

        // when
        Storage findStorage = storageRepository.findById(testStorage.getId()).orElse(
                Storage.builder().name("FAIL").build()
        );

        // then
        assertEquals(findStorage, testStorage);
        assertEquals(findStorage.getLocation().getX(), 12.12, 34.34);
        assertEquals(findStorage.getLocation().getY(), 12.12, 34.34);
    }

    @Test
    @Rollback(value = false)
    @DisplayName("findNearbyStorage: 좌표 기준으로 근처 창고 조회")
    public void findNearbyStorage() {
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

        System.out.println("d12(남산타워 ~ 경복궁) = " + d12); // 3093 m
        System.out.println("d23(경복궁 ~ 이순신장군) = " + d23); // 736 m
        System.out.println("d31(이순신장군 ~ 남산타워)= " + d31); // 2405 m

        storageRepository.save(testStorage1);
        storageRepository.save(testStorage2);
        storageRepository.save(testStorage3);
        // when
        List<Storage> result1 = storageRepository.findNearbyStorage(testStorage2.getLocation(), 2000);
        List<Storage> result2 = storageRepository.findNearbyStorage(testStorage1.getLocation(), 2500);

        // then
        assertTrue(result1.contains(testStorage3) && !result1.contains(testStorage1));
        assertTrue(result2.contains(testStorage3) && !result2.contains(testStorage2));
    }

    @Test
    @DisplayName("findByUserId: 요청한 회원의 id 로 보유 창고 조회")
    public void findByUserId() {
        // given
        User testUser = User.builder().userKey("test_1234").role(Role.USER).password("null").build();
        Storage testStorage1 = Storage.builder().name("test1").address("address1").location(factory.createPoint(new Coordinate(1.1, 2.2))).user(testUser).build();
        Storage testStorage2 = Storage.builder().name("test2").address("address2").location(factory.createPoint(new Coordinate(1.1, 2.2))).user(testUser).build();
        userRepository.save(testUser);
        storageRepository.save(testStorage1);
        storageRepository.save(testStorage2);

        // when
        List<Storage> findStorages = storageRepository.findByUserId(testUser.getId());

        // then
        assertEquals(findStorages.size(), 2);
    }
}