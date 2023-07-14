package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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
    private final GeometryFactory factory = new GeometryFactory();

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
                .location(factory.createPoint(new Coordinate(123.123, 456.456)))
                .user(testUser)
                .build();

        storageRepository.save(testStorage);

        // when
        Storage findStorage = storageRepository.findById(testStorage.getId()).orElse(
                Storage.builder().name("FAIL").build()
        );

        // then
        assertEquals(findStorage, testStorage);
        assertEquals(findStorage.getLocation().getX(), 123.123);
        assertEquals(findStorage.getLocation().getY(), 456.456);
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