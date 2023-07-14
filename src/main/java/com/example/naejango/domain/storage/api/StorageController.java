package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestServiceDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.domain.storage.dto.response.StorageListResponseDto;
import com.example.naejango.domain.storage.application.StorageService;
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

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;
    private final CommonDtoHandler commonDtoHandler;
    private final GeomUtil geomUtil;

    /**
     * 창고 생성 api
     * 창고를 생성하여 요청한 회원에게 할당하는 api
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
     * 내 창고 화면에서 창고 리스트의 각 창고 정보를 가져오는 api
     * @return StorageInfo (id, name, imgUrl, address)
     */
    @GetMapping("/")
    public ResponseEntity<StorageListResponseDto> StorageList(Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        List<Storage> storages = storageService.storageList(userId);
        return ResponseEntity.ok().body(new StorageListResponseDto(storages));
    }

    /**
     * 특정 창고의 상세 정보를 가져오는 api
     * @param storageId Long
     */
    @GetMapping("/{id}")
    public ResponseEntity<StorageInfoResponseDto> StorageInfo (@PathVariable("id") Long storageId) {
        StorageInfoResponseDto info = storageService.StorageInfo(storageId);
        return ResponseEntity.ok().body(info);
    }

    /**
     * 지도에서 추출된 위도 경도 값을 쿼리 파라미터로 받아,
     * 해당 좌표의 근처에 있는 창고를 리턴하는 메서드
     * Mysql 의 spatial 함수를 사용하거나 자바에서 하버사인 공식을 사용해야한다.
     * Elastic Search 를 사용하는 경우도 있다고 한다
     */
    @GetMapping("/nearby")
    public ResponseEntity<StorageNearbyListDto> StorageNearbyList (@RequestParam ("latitude") String latitude,
                                                                   @RequestParam("longitude") String longitude) {
        // 작성 예정
        return ResponseEntity.ok().body(new StorageNearbyListDto());
    }

}