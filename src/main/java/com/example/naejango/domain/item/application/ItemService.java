package com.example.naejango.domain.item.application;

import com.example.naejango.domain.chat.application.http.ChannelService;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.item.dto.SearchingCommandDto;
import com.example.naejango.domain.item.dto.request.CreateItemCommandDto;
import com.example.naejango.domain.item.dto.request.ModifyItemCommandDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.MatchResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.dto.MatchItemDto;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.transaction.repository.TransactionRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.wish.repository.WishRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
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
    private final ChannelService channelService;
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
        item.increaseViewCount();
        return new FindItemResponseDto(item);
    }

    /** 아이템 검색 */
    public List<SearchItemInfoDto> searchItem(SearchingCommandDto conditions) {
        Category category = findCategoryById(conditions.getCategoryId());

        List<SearchItemsDto> resultList = itemRepository.findItemsByConditions(conditions.getLocation(), conditions.getRad(), conditions.getPage(), conditions.getSize(),
                category, conditions.getKeyword(), conditions.getItemType(), conditions.getStatus());

        return resultList.stream().map(result -> new SearchItemInfoDto(result.getItem(), result.getStorage(), result.getCategory(), result.getDistance()))
                .collect(Collectors.toList());
    }

    /** 아이템 매칭 */
    public List<MatchResponseDto> matchItem(int rad, int size, Long itemId){
        // 아이템 조회
        Item item = itemRepository.findItemWithStorageById(itemId).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // 매칭 결과
        List<MatchItemDto> findResult = itemRepository.findMatchByCondition(item.getStorage().getLocation(), rad, size, item.getMatchingCondition());

        return findResult.stream().map(MatchItemDto::toResponseDto).collect(Collectors.toList());
    }

    /** 아이템 정보 수정 */
    @Transactional
    public ModifyItemResponseDto modifyItem(Long userId, Long itemId, ModifyItemCommandDto modifyItemCommandDto) {
        Category category = findCategoryById(modifyItemCommandDto.getCategory());
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        if (!userId.equals(item.getUser().getId())) { // 요청 보낸 유저와 아이템을 등록한 유저가 같은지 확인
            throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
        }

        modifyItemCommandDto.toEntity(item, category);

        Item savedItem = itemRepository.save(item);

        return new ModifyItemResponseDto(savedItem);
    }

    /** 아이템 삭제 */
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

        // 채널 종료
        closeChannel(userId, itemId);

        // 아이템 삭제
        itemRepository.delete(item);
    }

    private void closeChannel(Long userId, Long itemId) {
        // 아이템에 할당 된 채널 조회
        Optional<GroupChannel> optionalGroupChannel = channelService.findGroupChannelByItemId(itemId);

        // 채널이 존재 하면
        if (optionalGroupChannel.isPresent()) {
            GroupChannel groupChannel = optionalGroupChannel.get();

            // 종료 메세지 전송
            channelService.sendCloseMessage(groupChannel.getId(), userId);

            // 채널 종료
            groupChannel.closeChannel();
        }
    }

    private Item findItemWithCatById(Long itemId) { // category까지 fetch로 가져오는 메서드

        return itemRepository.findItemWithCategoryById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
    }

    private Category findCategoryByName(String categoryName) {

        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }
    private Category findCategoryById(Integer categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }


}
