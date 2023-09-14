package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager em;

//    @Nested
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    @DisplayName("Service 아이템 생성")
//    @WithMockUser()
//    class createItem {
//
//        User user = User.builder().id(1L).userKey("TEST_1234").build();
//        Category category = new Category();
//        Storage storage = Storage.builder().id(1L).build();
//        CreateItemRequestDto createItemRequestDto =
//                CreateItemRequestDto.builder()
//                        .name("아이템 이름")
//                        .description("아이템 설명")
//                        .imgUrl("이미지 URL")
//                        .itemType(ItemType.INDIVIDUAL_SELL)
//                        .category("카테고리")
//                        .storageId(storage.getId())
//                        .build();
//
//        @Test
//        @Order(1)
//        @DisplayName("성공")
//        void 성공() {
//            // given
//            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);
//            BDDMockito.given(storageRepository.findById(storage.getId())).willReturn(Optional.of(storage));
//            BDDMockito.given(em.getReference(User.class, user.getId())).willReturn(user);
//
//            // when
//            CreateItemResponseDto createItemResponseDto = itemService.createItem(user.getId(), createItemRequestDto);
//
//            // then
//            Assertions.assertEquals(createItemResponseDto, new CreateItemResponseDto(createItemRequestDto.toEntity(user, category)));
//            verify(itemRepository).save(any());
//
//            log.info(createItemResponseDto.toString());
//        }
//
//        @Test
//        @Order(2)
//        @DisplayName("실패_잘못된_카테고리_이름으로_요청_예외처리")
//        void 실패_잘못된_카테고리_이름으로_요청_예외처리() {
//            // given
//            BDDMockito.given(categoryRepository.findByName(any())).willReturn(null);
//
//            // when & then
//            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
//                itemService.createItem(any(), createItemRequestDto);
//            });
//
//            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.CATEGORY_NOT_FOUND);
//
//            log.info(exception.getErrorCode().toString());
//        }
//
//        @Test
//        @Order(3)
//        @DisplayName("실패_등록되지_않은_창고_ID_값으로_요청_예외처리")
//        void 실패_창고_생성_전에_아이템_등록_요청_예외처리() {
//            // given
//            BDDMockito.given(categoryRepository.findByName(any())).willReturn(category);
//            BDDMockito.given(storageRepository.findById(storage.getId())).willReturn(Optional.empty());
//
//            // when & then
//            CustomException exception = Assertions.assertThrows(CustomException.class, ()-> {
//                itemService.createItem(any(), createItemRequestDto);
//            });
//
//            Assertions.assertEquals(exception.getErrorCode(), ErrorCode.STORAGE_NOT_FOUND);
//
//            log.info(exception.getErrorCode().toString());
//        }
//
//    }
}