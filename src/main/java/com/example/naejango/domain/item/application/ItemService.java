package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemStorage;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemStorageRepository itemStorageRepository;

    private final CategoryRepository categoryRepository;

    private final StorageRepository storageRepository;

    /** 아이템 등록 */
    @Transactional
    public CreateItemResponseDto createItem(User user, CreateItemRequestDto createItemRequestDto) {
        Category category = categoryRepository.findByName(createItemRequestDto.getCategory());
        Storage storage = storageRepository.findById(createItemRequestDto.getStorageId()).orElse(null);

        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (storage == null) {
            throw new CustomException(ErrorCode.STORAGE_NOT_FOUND);
        }


        Item item = createItemRequestDto.toEntity(user, category);

        ItemStorage itemStorage = ItemStorage.builder()
                .id(null)
                .storage(storage)
                .item(item)
                .build();

        Item savedItem = itemRepository.save(item);
        itemStorageRepository.save(itemStorage);

        return new CreateItemResponseDto(savedItem);
    }

    /** 아이템 수정 */
    @Transactional
    public void modifyItem(User user, ModifyItemRequestDto modifyItemRequestDto) {
        Category category = categoryRepository.findByName(modifyItemRequestDto.getCategory());
        Item item = itemRepository.findById(modifyItemRequestDto.getId()).orElse(null);

        if (item == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        modifyItemRequestDto.toEntity(item, category);
    }
}
