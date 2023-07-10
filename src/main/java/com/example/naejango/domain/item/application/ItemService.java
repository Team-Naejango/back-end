package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemStorage;
import com.example.naejango.domain.item.dto.request.ConnectItemRequestDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    private final ItemStorageRepository itemStorageRepository;

    private final CategoryRepository categoryRepository;

    private final StorageRepository storageRepository;

    private final UserRepository userRepository;

    /** 아이템 생성 */
    @Transactional
    public CreateItemResponseDto createItem(Long userId, CreateItemRequestDto createItemRequestDto) {
        Category category = categoryRepository.findByName(createItemRequestDto.getCategory());
        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Item item = createItemRequestDto.toEntity(user, category);

        connectItemToStorage(user.getId(), item, createItemRequestDto.getStorageIdList());

        Item savedItem = itemRepository.save(item);

        return new CreateItemResponseDto(savedItem);
    }

    /** 아이템 정보 수정 */
    @Transactional
    public ModifyItemResponseDto modifyItem(Long userId, ModifyItemRequestDto modifyItemRequestDto) {
        Category category = categoryRepository.findByName(modifyItemRequestDto.getCategory());
        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Item item = itemRepository.findById(modifyItemRequestDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        if (user != item.getUser()) { // 요청 보낸 유저와 아이템을 등록한 유저가 같은지 확인
            throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
        }

        modifyItemRequestDto.toEntity(item, category);

        Item savedItem = itemRepository.save(item);

        return new ModifyItemResponseDto(savedItem);
    }

    /** 아이템 창고 등록 수정 */
    @Transactional
    public void connectItem(Long userId, ConnectItemRequestDto connectItemRequestDto) {
        Item item = itemRepository.findById(connectItemRequestDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 요청 보낸 유저와 아이템을 등록한 유저가 같은지 확인
        if (user != item.getUser()) {
            throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
        }

        connectItemToStorage(user.getId(), item, connectItemRequestDto.getStorageIdList());
    }

    /** 아이템과 창고 연결 */
    @Transactional
    public void connectItemToStorage(Long userId, Item item, List<Long> storageIdList) {
        List<Storage> storageList = storageRepository.findByUserId(userId); // 현재 유저의 창고 목록
        if (storageList.isEmpty()) {
            throw new CustomException(ErrorCode.STORAGE_NOT_EXIST);
        }

        // 현재 유저의 창고 목록 중 등록 요청에 포함되지 않은 창고는 제거
        storageList.removeIf(storage -> !storageIdList.contains(storage.getId()));

        // 현재 유저의 창고 목록에 없는 ID값 요청은 예외처리
        for (Storage storage : storageList) {
            storageIdList.remove(storage.getId());
        }
        if(!storageIdList.isEmpty()){
            throw new CustomException(ErrorCode.STORAGE_NOT_FOUND);
        }

        List<ItemStorage> itemStorageList = new ArrayList<>();
        for (Storage storage : storageList) {
            ItemStorage itemStorage = ItemStorage.builder()
                    .id(null)
                    .storage(storage)
                    .item(item)
                    .build();
            itemStorageList.add(itemStorage);
        }

        itemStorageRepository.saveAll(itemStorageList);
    }
}
