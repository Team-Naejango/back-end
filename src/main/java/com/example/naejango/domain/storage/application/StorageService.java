package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestServiceDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final UserService userService;

    @Transactional
    public Long createStorage(CreateStorageRequestServiceDto requestDto, Long userId) {
        User persistenceUser = userService.findUser(userId);
        Storage storage = new Storage(requestDto);
        storageRepository.save(storage);
        storage.assignUser(persistenceUser);
        return storage.getId();
    }

    public List<Storage> storageList(Long userId) {
        return storageRepository.findByUserId(userId);
    }

    public List<Storage> storageNearby(Point center) {
        return storageRepository.findNearbyStorage(center, 1000);
    }

    public StorageInfoResponseDto StorageInfo(Long storageId) {
        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));
        return new StorageInfoResponseDto(storage);
    }





}