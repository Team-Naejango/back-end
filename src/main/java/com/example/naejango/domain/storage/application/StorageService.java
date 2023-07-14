package com.example.naejango.domain.storage.application;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestServiceDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
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
    public void createStorage(CreateStorageRequestServiceDto requestDto, Long userId) {
        User persistenceUser = userService.findUser(userId);
        Storage storage = new Storage(requestDto);
        storageRepository.save(storage);
        storage.assignUser(persistenceUser);
    }

    public List<Storage> storageList(Long userId) {
        return storageRepository.findByUserId(userId);
    }

    public StorageInfoResponseDto StorageInfo(Long storageId) {
        Storage storage = storageRepository.findById(storageId).orElseThrow(()->{
            throw new IllegalArgumentException("창고를 찾을 수 없습니다.");
        });
        return new StorageInfoResponseDto(storage);
    }



}