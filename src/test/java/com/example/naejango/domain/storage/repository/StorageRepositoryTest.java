package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemStorage;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.application.SearchingConditionDto;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.SearchStorageResultDto;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
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
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemStorageRepository itemStorageRepository;
    @Autowired
    private CategoryRepository categoryRepository;

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
    @DisplayName("userId 로 Storage 조회")
    void findByUserIdTest() {
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
                .location(geomUtil.createPoint(12.12, 34.34))
                .user(testUser)
                .build();

        Storage testStorage2 = Storage.builder()
                .name("2")
                .address("Address")
                .location(geomUtil.createPoint(56.56, 78.78))
                .user(testUser)
                .build();

        Storage testStorage3 = Storage.builder()
                .name("3")
                .address("Address")
                .location(geomUtil.createPoint(-12.12, -34.34))
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

    @Nested
    @DisplayName("창고 검색")
    class SearchStorageByConditions {

        @BeforeEach
        void setup() {
            Storage testStorage4 = Storage.builder().name("테스트 창고 4").location(geomUtil.createPoint(127.021, 37.491)).address("").build();
            Storage testStorage5 = Storage.builder().name("테스트 창고 5").location(geomUtil.createPoint(127.022, 37.492)).address("").build();
            Storage testStorage6 = Storage.builder().name("테스트 창고 6").location(geomUtil.createPoint(127.023, 37.493)).address("").build();
            Storage testStorage7 = Storage.builder().name("테스트 창고 7").location(geomUtil.createPoint(127.024, 37.494)).address("").build();
            Storage testStorage8 = Storage.builder().name("테스트 창고 8").location(geomUtil.createPoint(127.025, 37.495)).address("").build();

            storageRepository.save(testStorage4);
            storageRepository.save(testStorage5);
            storageRepository.save(testStorage6);
            storageRepository.save(testStorage7);
            storageRepository.save(testStorage8);

            Category cat1 = categoryRepository.findByName("의류");
            Category cat2 = categoryRepository.findByName("디지털기기");
            Category cat3 = categoryRepository.findByName("생필품");

            Item item1 = Item.builder().name("청바지").status(true).type(ItemType.BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item2 = Item.builder().name("자켓").status(true).type(ItemType.SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item3 = Item.builder().name("셔츠").status(false).type(ItemType.BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item4 = Item.builder().name("면바지").status(false).type(ItemType.SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item5 = Item.builder().name("모니터").status(true).type(ItemType.BUY).category(cat2).description("").imgUrl("").viewCount(0).build();
            Item item6 = Item.builder().name("휴지").status(true).type(ItemType.BUY).category(cat3).description("").imgUrl("").viewCount(0).build();

            itemRepository.save(item1);
            itemRepository.save(item2);
            itemRepository.save(item3);
            itemRepository.save(item4);
            itemRepository.save(item5);
            itemRepository.save(item6);

            ItemStorage itemStorage1 = ItemStorage.builder().item(item1).storage(testStorage4).build();
            ItemStorage itemStorage2 = ItemStorage.builder().item(item2).storage(testStorage5).build();
            ItemStorage itemStorage3 = ItemStorage.builder().item(item3).storage(testStorage6).build();
            ItemStorage itemStorage4 = ItemStorage.builder().item(item4).storage(testStorage7).build();
            ItemStorage itemStorage5 = ItemStorage.builder().item(item5).storage(testStorage8).build();
            ItemStorage itemStorage6 = ItemStorage.builder().item(item6).storage(testStorage5).build();

            itemStorageRepository.save(itemStorage1);
            itemStorageRepository.save(itemStorage2);
            itemStorageRepository.save(itemStorage3);
            itemStorageRepository.save(itemStorage4);
            itemStorageRepository.save(itemStorage5);
            itemStorageRepository.save(itemStorage6);
        }


        @Test
        @DisplayName("좌표, 반경 조건 적용")
        void test1() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            SearchingConditionDto conditions = new SearchingConditionDto(null, new String[]{}, null, null);

            // when
            List<SearchStorageResultDto> searchStorageResultDtos = storageRepository.searchStorageByConditions(center, 1000, 0, 5, conditions);

            // then
            assertEquals(searchStorageResultDtos.size(), 5); // 테스트 창고 5건
            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4"); // 가장 가까운 창고
        }

        @Test
        @DisplayName("카테고리 조건 적용")
        void test2() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category cat1 = categoryRepository.findByName("의류");
            SearchingConditionDto conditions = new SearchingConditionDto(cat1, new String[]{}, null, null);

            // when
            List<SearchStorageResultDto> searchStorageResultDtos = storageRepository.searchStorageByConditions(center, 1000, 0, 5, conditions);

            // then
            assertEquals(searchStorageResultDtos.size(), 4); // 테스트 창고 4건
            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4"); // 가장 가까운 창고
        }

        @Test
        @DisplayName("키워드 조건 적용")
        void test3() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            SearchingConditionDto conditions = new SearchingConditionDto(null, new String[]{"%바지%"}, null, null);

            // when
            List<SearchStorageResultDto> searchStorageResultDtos = storageRepository.searchStorageByConditions(center, 1000, 0, 5, conditions);

            // then
            assertEquals(searchStorageResultDtos.size(), 2); // 테스트 창고 4(청바지) 테스트 창고 7(면바지)
            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4");
        }

        @Test
        @DisplayName("타입, 상태 조건 적용")
        void test4() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            SearchingConditionDto conditions = new SearchingConditionDto(null, new String[]{}, ItemType.BUY, true);

            // when
            List<SearchStorageResultDto> searchStorageResultDtos = storageRepository.searchStorageByConditions(center, 1000, 0, 5, conditions);

            // then
            assertEquals(searchStorageResultDtos.size(), 3); // 테스트 창고 4, 5, 8
            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4");
        }

        @Test
        @DisplayName("모든 조건 다 적용")
        void test5() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category cat1 = categoryRepository.findByName("의류");
            SearchingConditionDto conditions = new SearchingConditionDto(cat1, new String[]{"%청바지%"}, ItemType.BUY, true);

            // when
            List<SearchStorageResultDto> searchStorageResultDtos = storageRepository.searchStorageByConditions(center, 1000, 0, 5, conditions);

            // then
            assertEquals(searchStorageResultDtos.size(), 1); // 테스트 창고 4
            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4");
        }


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
    @DisplayName("창고 ID로 유저 ID 조회")
    public void findUserIdByStorageId() {
        // given
        User testUser1 = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .signature("null")
                .build();

        User testUser2 = User.builder()
                .userKey("test_5678")
                .password("null")
                .role(Role.USER)
                .signature("null")
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