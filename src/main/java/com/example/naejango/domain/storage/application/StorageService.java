package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.chat.application.http.ChannelService;
import com.example.naejango.domain.follow.repository.FollowRepository;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.StorageAndDistanceDto;
import com.example.naejango.domain.storage.dto.StorageInfoDto;
import com.example.naejango.domain.storage.dto.StorageInfoWithDistanceDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.transaction.repository.TransactionRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.wish.repository.WishRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemRepository itemRepository;
    private final WishRepository wishRepository;
    private final TransactionRepository transactionRepository;
    private final FollowRepository followRepository;
    private final ChannelService channelService;
    private final GeomUtil geomUtil;
    private final EntityManager em;

    @Transactional
    public Long createStorage(String name, Coord location, String address, String description, String imgUrl, Long userId) {
        Point point = geomUtil.createPoint(location.getLongitude(), location.getLatitude());
        Storage storage = Storage.builder()
                .name(name)
                .location(point)
                .address(address)
                .description(description)
                .imgUrl(imgUrl)
                .user(em.getReference(User.class, userId))
                .build();
        storageRepository.save(storage);
        return storage.getId();
    }

    public Page<Item> findItemList(Long storageId, boolean status, int page, int size) {
        return itemRepository.findItemWithCategoryByStorageIdAndStatus(storageId, status, PageRequest.of(page, size));
    }

    public Long findUserIdByStorageId(Long storageId) {
        return storageRepository.findUserIdByStorageId(storageId);
    }

    public List<StorageInfoDto> myStorageList(Long userId) {
        List<Storage> findResult = storageRepository.findByUserId(userId);
        return findResult.stream().map(StorageInfoDto::new).collect(Collectors.toList());
    }

    public List<StorageInfoWithDistanceDto> searchStorage (Point center, int radius, int page, int size) {
        List<StorageAndDistanceDto> storages = storageRepository.findStorageNearby(center, radius, PageRequest.of(page, size));
        return storages.stream().map(storage -> new StorageInfoWithDistanceDto(storage.getStorage(), storage.getDistance())).collect(Collectors.toList());
    }

    @Transactional
    public void modifyStorageInfo(Long storageId, Long userId, String name, String imgUrl, String description) {
        // 참고 로드
        Storage storage = storageRepository.findById(storageId).orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));

        // 권한 인증
        if (!storage.getUser().getId().equals(userId)) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);

        // 수정
        storage.modify(name, imgUrl, description);
    }

    @Transactional
    public void deleteStorage(Long storageId, Long userId) {
        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));

        // 창고를 등록한 유저인지 체크
        if (!storage.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORAGE_NOT_FOUND);
        }

        deleteStorage(storageId, userId, storage);
    }

    @Transactional
    public void deleteStorageByUserId(Long userId){
        List<Storage> storageList = storageRepository.findByUserId(userId);

        storageList.forEach(storage -> {
            // 해당 창고와 연관된 Follow 삭제
            deleteStorage(storage.getId(), userId, storage);
        });
    }

    private void deleteStorage(Long storageId, Long userId, Storage storage) {
        // 해당 창고와 연관된 Follow 삭제
        followRepository.deleteByStorageId(storageId);

        // 해당 창고와 연관된 Item 삭제
        deleteRelatedItem(storageId, userId);

        // Storage 를 삭제합니다.
        storageRepository.delete(storage);
    }

    private void deleteRelatedItem(Long storageId, Long userId) {
        // 해당 창고에 등록된 아이템 ID List 조회
        List<Long> itemIdList = itemRepository.findItemIdListByStorageId(storageId);

        // 각 아이템에 연관된 Wish 삭제
        wishRepository.deleteByItemIdList(itemIdList);

        // 각 아이템에 연관된 Transaction과의 관계 끊기
        transactionRepository.updateItemToNullByItemIdList(itemIdList);

        // 각 아이템의 채널 종료
        for (Long itemId : itemIdList) {
            channelService.closeChannelByItemId(itemId, userId);
        }

        // 창고에 등록된 아이템들 삭제
        itemRepository.deleteByStorageId(storageId);
    }
}