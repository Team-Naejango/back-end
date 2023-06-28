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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import java.awt.*;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Import({ItemService.class, ItemRepository.class, ItemStorageRepository.class, CategoryRepository.class, StorageRepository.class})
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
    @DisplayName("아이템 생성")
    @WithMockUser()
    class createItem {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            Point point = new Point(0, 1);
            User user = new User();
            Category category = new Category(1, "카테고리");
            Storage storage = new Storage(1L, "a", "s", point, user);
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
                    .willReturn(category);
            BDDMockito.given(storageRepository.findById(any()))
                    .willReturn(Optional.of(storage));
            BDDMockito.given(itemRepository.save(any())).willReturn(requestCreateItem.toEntity(user, category));

            // when
            ResponseCreateItem responseCreateItem = itemService.createItem(user, requestCreateItem);

            // then
            Assertions.assertEquals(responseCreateItem, new ResponseCreateItem(requestCreateItem.toEntity(user,category)));
            verify(itemRepository).save(any());
            verify(itemStorageRepository).save(any());
        }

    }
}