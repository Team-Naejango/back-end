package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.item.repository.ItemStorageRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.response.StorageNearbyInfo;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.handler.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final UserService userService;
    private final GeomUtil geomUtil;
    private final ItemStorageRepository itemStorageRepository;

    @Transactional
    public Long createStorage(CreateStorageRequestDto requestDto, Long userId) {
        Point location = geomUtil.createPoint(requestDto.getCoord().getLongitude(), requestDto.getCoord().getLatitude());
        Storage storage = new Storage(requestDto, location);
        storageRepository.save(storage);
        User persistenceUser = userService.findUser(userId);
        storage.assignUser(persistenceUser);
        return storage.getId();
    }

    public List<Storage> myStorageList(Long userId) {
        return storageRepository.findByUserId(userId);
    }

    public List<StorageNearbyInfo> storageNearby(Point center, int radius, int limit, int page) {
        int offset = limit * (page - 1);
        return storageRepository.findStorageNearby(center, radius, offset, limit);
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