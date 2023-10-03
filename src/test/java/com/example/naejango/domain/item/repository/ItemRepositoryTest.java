package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.MatchItemDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    CategoryRepository categoryRepository;
    GeomUtil geomUtil = new GeomUtil();


    @Nested
    @DisplayName("아이템 조회")
    class FindItem {
        @Test
        @DisplayName("성공")
        void test1() {
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
    }

    @Nested
    @DisplayName("아이템 검색")
    class SearchItemByConditions {

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

            Category cat1 = categoryRepository.findByName("의류").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Category cat2 = categoryRepository.findByName("디지털기기").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Category cat3 = categoryRepository.findByName("생필품").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            Item item1 = Item.builder().storage(testStorage4).name("청바지").tag("청바지").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item2 = Item.builder().storage(testStorage5).name("자켓").tag("자켓").status(true).itemType(ItemType.INDIVIDUAL_SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item3 = Item.builder().storage(testStorage6).name("셔츠").tag("셔츠").status(false).itemType(ItemType.INDIVIDUAL_BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item4 = Item.builder().storage(testStorage7).name("면바지").tag("면바지").status(false).itemType(ItemType.INDIVIDUAL_SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item5 = Item.builder().storage(testStorage4).name("모니터").tag("모니터").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat2).description("").imgUrl("").viewCount(0).build();
            Item item6 = Item.builder().storage(testStorage5).name("휴지").tag("휴지").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat3).description("").imgUrl("").viewCount(0).build();

            itemRepository.save(item1);
            itemRepository.save(item2);
            itemRepository.save(item3);
            itemRepository.save(item4);
            itemRepository.save(item5);
            itemRepository.save(item6);

        }

        @Test
        @DisplayName("좌표, 반경 조건 적용")
        void test1() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);

            // when
            List<SearchItemsDto> itemsByConditions = itemRepository
                    .findItemsByConditions(center, 1000, 0, 10, null, new String[]{}, null, null);

            // then
            assertEquals(itemsByConditions.size(), 6); // 아이템 6건 모두 조회
        }

        @Test
        @DisplayName("카테고리 조건 적용")
        void test2() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category category = categoryRepository.findByName("의류").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            // when
            List<SearchItemsDto> itemsByConditions = itemRepository
                    .findItemsByConditions(center, 1000, 0, 10, category, new String[]{}, null, null);

            // then
            assertEquals(itemsByConditions.size(), 4); // 청바지, 자켓, 셔츠, 면바지
        }

        @Test
        @DisplayName("키워드 조건 적용")
        void test3() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);

            // when
            List<SearchItemsDto> itemsByConditions = itemRepository
                    .findItemsByConditions(center, 1000, 0, 5, null, new String[]{"%바지%"}, null, null);

            // then
            assertEquals(itemsByConditions.size(), 2); // 청바지, 면바지
        }

        @Test
        @DisplayName("타입, 상태 조건 적용")
        void test4() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);

            // when
            List<SearchItemsDto> itemsByConditions = itemRepository
                    .findItemsByConditions(center, 1000, 0, 5, null, new String[]{}, ItemType.INDIVIDUAL_BUY, true);

            // then
            assertEquals(itemsByConditions.size(), 4); // 청바지, 모니터, 휴지, 셔츠

        }

        @Test
        @DisplayName("모든 조건 다 적용")
        void test5() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category category = categoryRepository.findByName("의류").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            // when
            List<SearchItemsDto> itemsByConditions = itemRepository
                    .findItemsByConditions(center, 1000, 0, 5, category, new String[]{"%바지%"}, ItemType.INDIVIDUAL_BUY, true);

            // then
            assertEquals(itemsByConditions.size(), 1); // 청바지
        }

    }

    @Nested
    @DisplayName("아이템 매칭")
    class MatchItemByConditions {

        @BeforeEach
        void setup() {
            User user1 = User.builder().userKey("test_1").password("null").role(Role.GUEST).build();
            User user2 = User.builder().userKey("test_2").password("null").role(Role.GUEST).build();
            User user3 = User.builder().userKey("test_3").password("null").role(Role.GUEST).build();
            User user4 = User.builder().userKey("test_4").password("null").role(Role.GUEST).build();

            em.persist(user1);
            em.persist(user2);
            em.persist(user3);
            em.persist(user4);

            Storage testStorage4 = Storage.builder().name("테스트 창고 4").location(geomUtil.createPoint(127.021, 37.491)).address("").user(user1).build();
            Storage testStorage5 = Storage.builder().name("테스트 창고 5").location(geomUtil.createPoint(127.022, 37.492)).address("").user(user2).build();
            Storage testStorage6 = Storage.builder().name("테스트 창고 6").location(geomUtil.createPoint(127.023, 37.493)).address("").user(user3).build();
            Storage testStorage7 = Storage.builder().name("테스트 창고 7").location(geomUtil.createPoint(127.024, 37.494)).address("").user(user4).build();
            Storage testStorage8 = Storage.builder().name("테스트 창고 8").location(geomUtil.createPoint(127.025, 37.495)).address("").user(user1).build();

            storageRepository.save(testStorage4);
            storageRepository.save(testStorage5);
            storageRepository.save(testStorage6);
            storageRepository.save(testStorage7);
            storageRepository.save(testStorage8);

            Category cat1 = categoryRepository.findByName("의류").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Category cat2 = categoryRepository.findByName("디지털기기").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Category cat3 = categoryRepository.findByName("생필품").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            Item item1 = Item.builder().storage(testStorage4).name("청바지").tag("청바지").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item2 = Item.builder().storage(testStorage5).name("자켓").tag("자켓").status(true).itemType(ItemType.INDIVIDUAL_SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item3 = Item.builder().storage(testStorage6).name("셔츠").tag("셔츠").status(false).itemType(ItemType.INDIVIDUAL_BUY).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item4 = Item.builder().storage(testStorage7).name("면바지").tag("면바지").status(false).itemType(ItemType.INDIVIDUAL_SELL).category(cat1).description("").imgUrl("").viewCount(0).build();
            Item item5 = Item.builder().storage(testStorage4).name("모니터").tag("모니터").status(true).itemType(ItemType.INDIVIDUAL_BUY).category(cat2).description("").imgUrl("").viewCount(0).build();
            Item item6 = Item.builder().storage(testStorage5).name("휴지").tag("휴지").status(true).itemType(ItemType.GROUP_BUY).category(cat3).description("").imgUrl("").viewCount(0).build();

            itemRepository.save(item1);
            itemRepository.save(item2);
            itemRepository.save(item3);
            itemRepository.save(item4);
            itemRepository.save(item5);
            itemRepository.save(item6);

        }

        @Test
        @DisplayName("매칭 성공 : 개인 판매 -> 개인 구매 매치")
        void test1() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category category = categoryRepository.findByName("의류").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Item item = Item.builder()
                    .name("청바지")
                    .tag("청바지")
                    .category(category)
                    .itemType(ItemType.INDIVIDUAL_SELL)
                    .status(true)
                    .build();

            // when
            List<MatchItemDto> matchByCondition = itemRepository.findMatchByCondition(center, 1000, 5, item.getMatchingCondition());

            // then
            assertEquals(matchByCondition.size(), 1);
            assertEquals(matchByCondition.get(0).getItem().getName(), "청바지"); // 청바지
        }

        @Test
        @DisplayName("매칭 성공 : 개인 구매 -> 공동 구매 매치")
        void test2() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category category = categoryRepository.findByName("생필품").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Item item = Item.builder()
                    .name("휴지")
                    .tag("휴지")
                    .category(category)
                    .itemType(ItemType.INDIVIDUAL_BUY)
                    .status(true)
                    .build();

            // when
            List<MatchItemDto> matchByCondition = itemRepository.findMatchByCondition(center, 1000, 5, item.getMatchingCondition());

            // then
            assertEquals(matchByCondition.size(), 1); // 휴지
            assertEquals(matchByCondition.get(0).getItem().getName(), "휴지");
        }

        @Test
        @DisplayName("매칭 실패")
        void test3() {
            // given
            Point center = geomUtil.createPoint(127.02, 37.49);
            Category category = categoryRepository.findByName("의류").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            Item item = Item.builder()
                    .name("자켓")
                    .tag("자켓 청자켓 리바이스")
                    .category(category)
                    .itemType(ItemType.INDIVIDUAL_SELL)
                    .status(true)
                    .build();

            // when
            List<MatchItemDto> matchByCondition = itemRepository.findMatchByCondition(center, 1000, 5, item.getMatchingCondition());

            // then
            assertEquals(matchByCondition.size(), 0); // 자켓
        }

    }

    @Nested
    @DisplayName("아이템 삭제")
    class DeleteItem {
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
        }
    }


}