package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.RequestCreateItem;
import com.example.naejango.domain.item.dto.response.ResponseCreateItem;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemStorageRepository itemStorageRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StorageRepository storageRepository;


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("아이템 생성")
    @WithMockUser()
    class createItem {
        @Test
        @Order(1)
        @DisplayName("성공")
        void 성공() {
            // given
            User user = new User();
            Category category = new Category();
            Storage storage = new Storage();

            RequestCreateItem requestCreateItem = RequestCreateItem
                    .builder()
                    .category("카테고리")
                    .name("아이템 이름")
                    .description("아이템 설명")
                    .imgUrl("이미지 URL")
                    .type(ItemType.SELL)
                    .StorageId(1L)
                    .build();

            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);

            BDDMockito.given(storageRepository.findById(any())).willReturn(Optional.of(storage));

            BDDMockito.given(itemRepository.save(any())).willReturn(requestCreateItem.toEntity(user, category));

            // when
            ResponseCreateItem responseCreateItem = itemService.createItem(user, requestCreateItem);

            // then
            Assertions.assertEquals(responseCreateItem, new ResponseCreateItem(requestCreateItem.toEntity(user, category)));
            verify(itemRepository).save(any());
            verify(itemStorageRepository).save(any());

            log.info(responseCreateItem.toString());
        }

        @Test
        @Order(2)
        @DisplayName("실패_카테고리만_Null")
        void 실패_카테고리만_Null() {
            // given
            User user = new User();
            Storage storage = new Storage();

            RequestCreateItem requestCreateItem = RequestCreateItem
                    .builder()
                    .category("카테고리")
                    .name("아이템 이름")
                    .description("아이템 설명")
                    .imgUrl("이미지 URL")
                    .type(ItemType.SELL)
                    .StorageId(1L)
                    .build();

            BDDMockito.given(categoryRepository.findByName(any()))
                    .willReturn(null);

            BDDMockito.given(storageRepository.findById(any()))
                    .willReturn(Optional.of(storage));

            // when & then
            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
                itemService.createItem(user, requestCreateItem);
            });

            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.CATEGORY_NOT_FOUND);

            log.info(exception.getErrorCode().toString());
        }

        @Test
        @Order(3)
        @DisplayName("실패_창고만_Null")
        void 실패_창고만_Null() {
            // given
            User user = new User();
            Category category = new Category();

            RequestCreateItem requestCreateItem = RequestCreateItem
                    .builder()
                    .category("카테고리")
                    .name("아이템 이름")
                    .description("아이템 설명")
                    .imgUrl("이미지 URL")
                    .type(ItemType.SELL)
                    .StorageId(1L)
                    .build();


            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);

            BDDMockito.given(storageRepository.findById(any()))
                    .willReturn(Optional.empty());

            // when & then
            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
                itemService.createItem(user, requestCreateItem);
            });

            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.STORAGE_NOT_FOUND);

            log.info(exception.getErrorCode().toString());
        }
    }
}