package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.application.SearchingConditionDto;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final StorageRepository storageRepository;
    private final UserRepository userRepository;
    private final EntityManager em;

    /** 아이템 생성 */
    @Transactional
    public CreateItemResponseDto createItem(Long userId, CreateItemRequestDto createItemRequestDto) {
        Category category = validateCategory(createItemRequestDto.getCategory());
        Storage storage = storageRepository.findById(createItemRequestDto.getStorageId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));

        Item item = createItemRequestDto.toEntity(em.getReference(User.class, userId), category);
        item.putItem(storage);

        itemRepository.save(item);
        return new CreateItemResponseDto(item);
    }

    /** 아이템 정보 조회 */
    public FindItemResponseDto findItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        return new FindItemResponseDto(item);
    }

    /** 아이템 검색 */
    public List<SearchItemInfoDto> searchItem(Point center, int rad, int page, int size, SearchingConditionDto conditions) {
        List<SearchItemsDto> resultList = itemRepository.findItemsByConditions(center, rad, page, size, conditions);
        return resultList.stream().map(result -> new SearchItemInfoDto(result.getItem(), result.getStorage(), result.getCategory(), result.getDistance()))
                .collect(Collectors.toList());
    }

    /** 아이템 정보 수정 */
    @Transactional
    public ModifyItemResponseDto modifyItem(Long userId, Long itemId, ModifyItemRequestDto modifyItemRequestDto) {
        Category category = validateCategory(modifyItemRequestDto.getCategory());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        if (user != item.getUser()) { // 요청 보낸 유저와 아이템을 등록한 유저가 같은지 확인
            throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
        }

        modifyItemRequestDto.toEntity(item, category);

        Item savedItem = itemRepository.save(item);

        return new ModifyItemResponseDto(savedItem);
    }

    private Category validateCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName);
        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }

}
