package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.response.StorageNearbyDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.handler.GeomUtil;
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
    private final GeomUtil geomUtil;

    @Transactional
    public Long createStorage(CreateStorageRequestDto requestDto, Long userId) {
        Point location = geomUtil.createPoint(requestDto.getLongitude(), requestDto.getLatitude());
        Storage storage = new Storage(requestDto, location);
        storageRepository.save(storage);
        User persistenceUser = userService.findUser(userId);
        storage.assignUser(persistenceUser);
        return storage.getId();
    }

    public List<Storage> myStorageList(Long userId) {
        return storageRepository.findByUserId(userId);
    }

    public List<StorageNearbyDto> storageNearby(Point center, int radius, int limit, int page) {
        int offset = limit * (page - 1);
        return storageRepository.findStorageNearby(center, radius, offset, limit);
    }

    public int countStorageNearby(Point center, int radius) {
        return storageRepository.countStorageWithinRadius(center, radius);
    }

}