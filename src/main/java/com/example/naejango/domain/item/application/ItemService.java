package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.item.dto.request.CreateItemCommandDto;
import com.example.naejango.domain.item.dto.request.ModifyItemCommandDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.SearchingConditionDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.transaction.repository.TransactionRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.wish.repository.WishRepository;
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
    private final WishRepository wishRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManager em;

    /** 아이템 생성 */
    @Transactional
    public CreateItemResponseDto createItem(Long userId, CreateItemCommandDto createItemCommandDto) {
        Category category = findCategoryByName(createItemCommandDto.getCategory());

        // 본인이 등록한 창고에만 아이템 등록 가능
        Storage storage = storageRepository.findByIdAndUserId(createItemCommandDto.getStorageId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));

        Item item = createItemCommandDto.toEntity(em.getReference(User.class, userId), storage, category);

        itemRepository.save(item);

        return new CreateItemResponseDto(item);
    }

    /** 아이템 정보 조회 */
    public FindItemResponseDto findItem(Long itemId) {
        Item item = findItemWithCatById(itemId);

        return new FindItemResponseDto(item);
    }

    /** 아이템 검색 */
    public List<SearchItemInfoDto> searchItem(Point center, int rad, int page, int size, SearchingConditionDto conditions) {
        Category category = findCategoryByName(conditions.getCategory());

        List<SearchItemsDto> resultList = itemRepository.findItemsByConditions(center, rad, page, size, category, conditions);

        return resultList.stream().map(result -> new SearchItemInfoDto(result.getItem(), result.getStorage(), result.getCategory(), result.getDistance()))
                .collect(Collectors.toList());
    }

    /** 아이템 정보 수정 */
    @Transactional
    public ModifyItemResponseDto modifyItem(Long userId, Long itemId, ModifyItemCommandDto modifyItemCommandDto) {
        Category category = findCategoryByName(modifyItemCommandDto.getCategory());
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        if (!userId.equals(item.getUser().getId())) { // 요청 보낸 유저와 아이템을 등록한 유저가 같은지 확인
            throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
        }

        modifyItemCommandDto.toEntity(item, category);

        Item savedItem = itemRepository.save(item);

        return new ModifyItemResponseDto(savedItem);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // 아이템을 등록한 유저인지 체크
        if (!item.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
        }

        // 해당 아이템에 연관된 Wish 삭제
        wishRepository.deleteByItemId(itemId);

        // 해당 아이템에 연관된 Transaction과의 관계 끊기
        transactionRepository.updateItemToNullByItemId(itemId);

        // 아이템 삭제
        itemRepository.delete(item);
    }

    private Item findItemWithCatById(Long itemId) { // category까지 fetch로 가져오는 메서드

        return itemRepository.findItemById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
    }

    private Category findCategoryByName(String categoryName) {

        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

}
