package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Mock
    private UserRepository userRepository;

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Service 아이템 생성")
    @WithMockUser()
    class createItem {
        User user = User.builder().id(1L).userKey("TEST_1234").build();
        Category category = new Category();
        Storage storage1 = Storage.builder().id(1L).build();
        Storage storage2 = Storage.builder().id(2L).build();
        Storage storage3 = Storage.builder().id(3L).build();
        CreateItemRequestDto createItemRequestDto =
                CreateItemRequestDto.builder()
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .type(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .storageIdList(new ArrayList<>(List.of(1L, 2L)))
                        .build();

        @Test
        @Order(1)
        @DisplayName("성공")
        void 성공() {
            // given
            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);
            BDDMockito.given(storageRepository.findByUserId(any())).willReturn(new ArrayList<>(List.of(storage1, storage2, storage3)));
            BDDMockito.given(itemRepository.save(any())).willReturn(createItemRequestDto.toEntity(user, category));
            BDDMockito.given(userRepository.findById(any())).willReturn(Optional.of(user));

            // when
            CreateItemResponseDto createItemResponseDto = itemService.createItem(any(), createItemRequestDto);

            // then
            Assertions.assertEquals(createItemResponseDto, new CreateItemResponseDto(createItemRequestDto.toEntity(user, category)));
            verify(itemRepository).save(any());
            verify(itemStorageRepository).saveAll(any());

            log.info(createItemResponseDto.toString());
        }

        @Test
        @Order(2)
        @DisplayName("실패_잘못된_카테고리_이름으로_요청_예외처리")
        void 실패_잘못된_카테고리_이름으로_요청_예외처리() {
            // given
            BDDMockito.given(categoryRepository.findByName(any())).willReturn(null);

            // when & then
            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
                itemService.createItem(any(), createItemRequestDto);
            });

            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.CATEGORY_NOT_FOUND);

            log.info(exception.getErrorCode().toString());
        }

        @Test
        @Order(3)
        @DisplayName("실패_창고_생성_전에_아이템_등록_요청_예외처리")
        void 실패_창고_생성_전에_아이템_등록_요청_예외처리() {
            // given
            BDDMockito.given(userRepository.findById(any())).willReturn(Optional.of(user));
            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);
            BDDMockito.given(storageRepository.findByUserId(any())).willReturn(new ArrayList<>(Collections.emptyList()));
            // when & then
            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
                itemService.createItem(any(), createItemRequestDto);
            });

            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.STORAGE_NOT_EXIST);

            log.info(exception.getErrorCode().toString());
        }

        @Test
        @Order(4)
        @DisplayName("실패_등록되지_않은_창고_ID_값으로_요청_예외처리")
        void 실패_등록되지_않은_창고_ID_값으로_요청_예외처리() {
            // given
            BDDMockito.given(userRepository.findById(any())).willReturn(Optional.of(user));
            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);
            BDDMockito.given(storageRepository.findByUserId(any())).willReturn(new ArrayList<>(List.of(storage1)));
            // when & then
            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
                itemService.createItem(any(), createItemRequestDto);
            });

            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.STORAGE_NOT_FOUND);

            log.info(exception.getErrorCode().toString());
        }
    }
}