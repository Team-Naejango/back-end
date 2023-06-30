package com.example.naejango.domain.storage.service;

import com.example.naejango.domain.storage.domain.Location;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final UserService userService;

    @Transactional
    public void createStorage(CreateStorageRequestDto requestDto, Authentication authentication) {
        User user = userService.getUser(authentication);

        Storage newStorage = Storage.builder()
                .name(requestDto.getName())
                .imgUrl(requestDto.getImgUrl())
                .description(requestDto.getDescription())
                .address(requestDto.getAddress())
                .location(Location.builder()
                        .latitude(requestDto.getLatitude())
                        .longitude(requestDto.getLongitude())
                        .build())
                .user(user)
                .build();

        storageRepository.save(newStorage);
    }

    public StorageInfoResponseDto info(Long storageId) {
        Storage storage = storageRepository.findById(storageId).orElseThrow(()->{
            throw new IllegalArgumentException("창고를 찾을 수 없습니다.");
        });

        return StorageInfoResponseDto.builder()
                .name(storage.getName())
                .address(storage.getAddress())
                .imgUrl(storage.getImgUrl())
                .description(storage.getDescription())
                .longitude(storage.getLocation().getLongitude())
                .latitude(storage.getLocation().getLatitude())
                .build();
    }
}