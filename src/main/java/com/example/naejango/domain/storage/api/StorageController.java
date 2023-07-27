package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestServiceDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.domain.storage.dto.response.StorageListResponseDto;
import com.example.naejango.domain.storage.dto.response.StorageNearbyListDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import com.example.naejango.global.common.handler.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;
    private final CommonDtoHandler commonDtoHandler;
    private final GeomUtil geomUtil;

    /**
     * 창고 생성 및 요청한 회원에게 할당
     */
    @PostMapping("/")
    public ResponseEntity<Void> createStorage(@RequestBody CreateStorageRequestDto requestDto, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        Point storageLocation = geomUtil.createPoint(requestDto.getLatitude(), requestDto.getLongitude());
        CreateStorageRequestServiceDto serviceDto = requestDto.toServiceDto(storageLocation);
        Long storageId = storageService.createStorage(serviceDto, userId);

        String storageUri = "/api/storage/" + storageId.toString();
        return ResponseEntity.created(URI.create(storageUri)).body(null);
    }

    /**
     * 내 창고 화면에서 창고 리스트의 각 창고 정보 조회
     * @return StorageInfo (id, name, imgUrl, address)
     */
    @GetMapping("/")
    public ResponseEntity<StorageListResponseDto> storageList(Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        List<Storage> storages = storageService.storageList(userId);
        return ResponseEntity.ok().body(new StorageListResponseDto(storages));
    }

    /**
     * 위도, 경도, 반경 값을 쿼리 파라미터로 받아 해당 좌표 근처의 창고 조회
     * 추후 JPQL 또는 DSL 등 학습하여 정렬 및 페이징 처리필요
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<StorageNearbyListDto>> storageNearbyList(@RequestParam("longitude") double longitude,
                                                                        @RequestParam("latitude") double latitude) {
        Point centerPoint = geomUtil.createPoint(longitude, latitude);
        List<Storage> storagesNearby = storageService.storageNearby(centerPoint);

        List<StorageNearbyListDto> storageNearbyList = storagesNearby.stream().map(s -> {
            int distance = geomUtil.calculateDistance(s.getLocation(), centerPoint);
            return new StorageNearbyListDto(s, distance);
        }).collect(Collectors.toList());

        return ResponseEntity.ok().body(storageNearbyList);
    }

    /**
     * 특정 창고의 상세 정보 조회
     * @param storageId Long
     */
    @GetMapping("/{id}")
    public ResponseEntity<StorageInfoResponseDto> storageInfo(@PathVariable("id") Long storageId) {
        StorageInfoResponseDto info = storageService.StorageInfo(storageId);
        return ResponseEntity.ok().body(info);
    }


}