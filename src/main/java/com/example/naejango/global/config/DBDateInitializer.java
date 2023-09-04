package com.example.naejango.global.config;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemStorage;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.util.RandomDataGenerateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "initialize-db", havingValue = "true")
@RequiredArgsConstructor
public class DBDateInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;
    private final StorageRepository storageRepository;
    private final TransactionTemplate transactionTemplate;
    private final ItemRepository itemRepository;
    private final ItemStorageRepository itemStorageRepository;
    private final RandomDataGenerateUtil randomUtil;

    private final List<Long> userIdList = new ArrayList<>();
    private final List<Integer> catIdList = new ArrayList<>();

    @PersistenceContext EntityManager em;

    @Override
    public void run(ApplicationArguments args) {
        createBasicUser(10);
        createCategory();
        createStorageAndItem(10);
    }

    @Transactional
    void createBasicUser(int n) {
        transactionTemplate.execute(status -> {
            for (int i = 0; i < n; i++) {
                    // 유저 생성
                    User testUser = User.builder().role(Role.USER).userKey(UUID.randomUUID().toString()).password("").build();
                    userRepository.save(testUser);
                    userIdList.add(testUser.getId());

                    // 유저 프로필 생성
                    UserProfile userProfile = UserProfile.builder().nickname(randomUtil.getRandomNickname())
                            .imgUrl(randomUtil.getRandomImageUrl()).birth(randomUtil.getRandomBirth())
                            .gender(randomUtil.getRandomGender())
                            .phoneNumber("01012345678").intro("테스트 회원입니다.").build();

                    userProfileRepository.save(userProfile);

                    // 유저 프로필 연결
                    testUser.setUserProfile(userProfile);
            }
            return null;
        });
    }

    @Transactional
    void createCategory() {
        Category cat1 = Category.builder().name("생필품").build();
        Category cat2 = Category.builder().name("의류").build();
        Category cat3 = Category.builder().name("가구").build();
        Category cat4 = Category.builder().name("디지털기기").build();
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);
        categoryRepository.save(cat3);
        categoryRepository.save(cat4);
        catIdList.add(cat1.getId());
        catIdList.add(cat2.getId());
        catIdList.add(cat3.getId());
        catIdList.add(cat4.getId());
    }

    @Transactional
    void createStorageAndItem(int itemN) {
        transactionTemplate.execute(status -> {
            for (Long useId : userIdList) {
                User testUser = em.getReference(User.class, useId);

                for (int j = 0; j < randomUtil.getRandomInt(4); j++) {
                    Storage testStorage = Storage.builder().name(randomUtil.getRandomStorageName())
                            .location(randomUtil.getRandomPointInGangnam())
                            .user(testUser)
                            .description("테스트 창고 입니다.")
                            .imgUrl(randomUtil.getRandomImageUrl())
                            .address("서울시 강남구")
                            .build();
                    storageRepository.save(testStorage);

                    for (int k = 0; k < randomUtil.getRandomInt(itemN); k++) {
                        Category category = em.getReference(Category.class, catIdList.get(randomUtil.getRandomInt(4)));
                        String randomItemName = randomUtil.getRandomItemName();
                        ItemType randomItemType = randomUtil.getRandomItemType();
                        Item testItem = Item.builder().category(category)
                                .type(randomUtil.getRandomItemType())
                                .user(testUser)
                                .status(randomUtil.getRandomBoolean())
                                .name(randomItemName)
                                .description(randomUtil.getItemDescription(randomItemName, randomItemType))
                                .imgUrl("")
                                .user(testUser)
                                .viewCount(randomUtil.getRandomInt(100)).build();
                        itemRepository.save(testItem);

                        ItemStorage itemStorage = ItemStorage.builder().item(testItem).storage(testStorage).build();
                        itemStorageRepository.save(itemStorage);
                    }

                }
            }
            return null;
        });
    }
}
