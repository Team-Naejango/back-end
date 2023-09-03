package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemRepository itemRepository;
    private final GeomUtil geomUtil;
    private final ItemStorageRepository itemStorageRepository;
    private final EntityManager em;

    @Transactional
    public Long createStorage(String name, Coord location, String address, String description, String imgUrl,  Long userId) {
        Point point = geomUtil.createPoint(location.getLongitude(), location.getLatitude());
        User user = em.getReference(User.class, userId);
        Storage storage = Storage.builder()
                .name(name)
                .location(point)
                .address(address)
                .description(description)
                .imgUrl(imgUrl)
                .user(user)
                .build();
        storageRepository.save(storage);
        return storage.getId();
    }

    public List<ItemInfoDto> findItemList(Long storageId, Boolean status, int page, int size) {
        Page<ItemInfoDto> items = itemRepository.findByStorageId(storageId, status, PageRequest.of(page, size));
        return items.getContent();
    }

    public List<Storage> myStorageList(Long userId) {
        return storageRepository.findByUserId(userId);
    }

    public List<StorageNearbyInfoDto> storageNearby(Point center, int radius, int page, int size) {
        return storageRepository.findStorageNearby(center, radius, page, size);
    }

    public int countStorageNearby(Point center, int radius) {
        return storageRepository.countStorageWithinRadius(center, radius);
    }

    @Transactional
    public void modifyStorageInfo(ModifyStorageInfoRequestDto requestDto, Long storageId, Long userId) {
        Storage storage = storageRepository.findById(storageId).orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));
        if (!storage.getUser().getId().equals(userId)) throw new CustomException(ErrorCode.UNAUTHORIZED_MODIFICATION_REQUEST);
        storage.modify(requestDto);
    }

    @Transactional
    public void deleteStorage(Long storageId, Long userId) {
        Storage deleteRequestedStorage = storageRepository.findById(storageId).orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));
        List<Storage> userStorageList = storageRepository.findByUserId(userId);

        Optional<Storage> matchedStorage = userStorageList.stream().filter(storage -> storage.getId().equals(deleteRequestedStorage.getId())).findAny();
        if(matchedStorage.isEmpty()) throw new CustomException(ErrorCode.UNAUTHORIZED_DELETE_REQUEST);

        Storage deleteStorage = matchedStorage.get();
        itemStorageRepository.deleteByStorageId(deleteStorage.getId());
        storageRepository.delete(deleteStorage);
    }
}