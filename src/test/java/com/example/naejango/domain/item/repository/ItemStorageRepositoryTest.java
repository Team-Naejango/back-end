package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemStorageRepositoryTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StorageRepository storageRepository;

    GeomUtil geomUtil = new GeomUtil();


    @Test
    @DisplayName("userId 로 Storage 조회")
    void findByUserIdTest() {
        // given
        User testUser = User.builder()
                .userKey("test_1234")
                .password("null")
                .role(Role.USER)
                .refreshToken("null")
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

//    @Nested
//    @DisplayName("창고 검색")
//    class SearchStorageByConditions {
//
//        @BeforeEach
//        void setup() {
//            Storage testStorage4 = Storage.builder().name("테스트 창고 4").location(geomUtil.createPoint(127.021, 37.491)).address("").build();
//            Storage testStorage5 = Storage.builder().name("테스트 창고 5").location(geomUtil.createPoint(127.022, 37.492)).address("").build();
//            Storage testStorage6 = Storage.builder().name("테스트 창고 6").location(geomUtil.createPoint(127.023, 37.493)).address("").build();
//            Storage testStorage7 = Storage.builder().name("테스트 창고 7").location(geomUtil.createPoint(127.024, 37.494)).address("").build();
//            Storage testStorage8 = Storage.builder().name("테스트 창고 8").location(geomUtil.createPoint(127.025, 37.495)).address("").build();
//
//            storageRepository.save(testStorage4);
//            storageRepository.save(testStorage5);
//            storageRepository.save(testStorage6);
//            storageRepository.save(testStorage7);
//            storageRepository.save(testStorage8);
//
//            Category cat1 = categoryRepository.findByName("의류");
//            Category cat2 = categoryRepository.findByName("디지털기기");
//            Category cat3 = categoryRepository.findByName("생필품");
//
//            Item item1 = Item.builder().storage(testStorage4).name("청바지").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
//            Item item2 = Item.builder().storage(testStorage5).name("자켓").status(true).itemType(ItemType.INDIVIDUAL_SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
//            Item item3 = Item.builder().storage(testStorage6).name("셔츠").status(false).itemType(ItemType.INDIVIDUAL_BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
//            Item item4 = Item.builder().storage(testStorage7).name("면바지").status(false).itemType(ItemType.INDIVIDUAL_SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
//            Item item5 = Item.builder().storage(testStorage4).name("모니터").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat2).description("").imgUrl("").viewCount(0).build();
//            Item item6 = Item.builder().storage(testStorage5).name("휴지").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat3).description("").imgUrl("").viewCount(0).build();
//
//            itemRepository.save(item1);
//            itemRepository.save(item2);
//            itemRepository.save(item3);
//            itemRepository.save(item4);
//            itemRepository.save(item5);
//            itemRepository.save(item6);
//
//        }
//
//        @Test
//        @DisplayName("좌표, 반경 조건 적용")
//        void test1() {
//            // given
//            Point center = geomUtil.createPoint(127.02, 37.49);
//            SearchingConditionDto conditions = new SearchingConditionDto(null, new String[]{}, null, null);
//
//            // when
//            List<StorageAndDistanceDto> searchStorageResultDtos = storageRepository.searchItemsByConditions(center, 1000, 0, 5, conditions);
//
//            // then
//            assertEquals(searchStorageResultDtos.size(), 5); // 테스트 창고 5건
//            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4"); // 가장 가까운 창고
//        }
//
//        @Test
//        @DisplayName("카테고리 조건 적용")
//        void test2() {
//            // given
//            Point center = geomUtil.createPoint(127.02, 37.49);
//            Category cat1 = categoryRepository.findByName("의류");
//            SearchingConditionDto conditions = new SearchingConditionDto(cat1, new String[]{}, null, null);
//
//            // when
//            List<StorageAndDistanceDto> searchStorageResultDtos = storageRepository.searchItemsByConditions(center, 1000, 0, 5, conditions);
//
//            // then
//            assertEquals(searchStorageResultDtos.size(), 4); // 테스트 창고 4건
//            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4"); // 가장 가까운 창고
//        }
//
//        @Test
//        @DisplayName("키워드 조건 적용")
//        void test3() {
//            // given
//            Point center = geomUtil.createPoint(127.02, 37.49);
//            SearchingConditionDto conditions = new SearchingConditionDto(null, new String[]{"%바지%"}, null, null);
//
//            // when
//            List<StorageAndDistanceDto> searchStorageResultDtos = storageRepository.searchItemsByConditions(center, 1000, 0, 5, conditions);
//
//            // then
//            assertEquals(searchStorageResultDtos.size(), 2); // 테스트 창고 4(청바지) 테스트 창고 7(면바지)
//            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4");
//        }
//
//        @Test
//        @DisplayName("타입, 상태 조건 적용")
//        void test4() {
//            // given
//            Point center = geomUtil.createPoint(127.02, 37.49);
//            SearchingConditionDto conditions = new SearchingConditionDto(null, new String[]{}, ItemType.INDIVIDUAL_BUY, true);
//
//            // when
//            List<StorageAndDistanceDto> searchStorageResultDtos = storageRepository.searchItemsByConditions(center, 1000, 0, 5, conditions);
//
//            // then
//            assertEquals(searchStorageResultDtos.size(), 3); // 테스트 창고 4, 5, 8
//            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4");
//        }
//
//        @Test
//        @DisplayName("모든 조건 다 적용")
//        void test5() {
//            // given
//            Point center = geomUtil.createPoint(127.02, 37.49);
//            Category cat1 = categoryRepository.findByName("의류");
//            SearchingConditionDto conditions = new SearchingConditionDto(cat1, new String[]{"%청바지%"}, ItemType.INDIVIDUAL_BUY, true);
//
//            // when
//            List<StorageAndDistanceDto> searchStorageResultDtos = storageRepository.searchItemsByConditions(center, 1000, 0, 5, conditions);
//
//            // then
//            assertEquals(searchStorageResultDtos.size(), 1); // 테스트 창고 4
//            assertEquals(searchStorageResultDtos.get(0).getName(), "테스트 창고 4");
//        }
//
//    }

    @Test
    @DisplayName("")
    void deleteByStorageIdTest(){
        // given
        Storage storage1 = Storage.builder().name("test1").description("").address("").imgUrl("").location(geomUtil.createPoint(1, 1)).build();
        Storage storage2 = Storage.builder().name("test2").description("").address("").imgUrl("").location(geomUtil.createPoint(1, 1)).build();
        Item item1 = Item.builder().status(true).itemType(ItemType.INDIVIDUAL_BUY).name("item1").imgUrl("").viewCount(0).tag("태그1 태그2").description("").build();
        Item item2 = Item.builder().status(true).itemType(ItemType.INDIVIDUAL_BUY).name("item1").imgUrl("").viewCount(0).tag("태그3 태그4").description("").build();
        storageRepository.save(storage1);
        storageRepository.save(storage2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        em.flush();
        em.clear();

        // when
        
        // then
        Storage result1 = storageRepository.findById(storage1.getId()).orElseGet(()->Storage.builder().name("실패").build());
        Storage result2 = storageRepository.findById(storage2.getId()).orElseGet(()->Storage.builder().name("실패").build());
    }

}