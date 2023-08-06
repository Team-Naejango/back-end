package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.response.MyStorageListResponseDto;
import com.example.naejango.domain.storage.dto.response.StorageNearbyInfo;
import com.example.naejango.domain.storage.dto.response.StorageNearbyListResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import com.example.naejango.global.common.handler.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
     * 창고 생성 및 요청한 회원에게 할당
     */
    @PostMapping("")
    public ResponseEntity<Void> createStorage(@RequestBody @Valid CreateStorageRequestDto requestDto, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        Long storageId = storageService.createStorage(requestDto, userId);

        String storageUri = "/api/storage/" + storageId.toString();
        return ResponseEntity.created(URI.create(storageUri)).body(null);
    }

    /**
     * 나의 창고 리스트 조회
     *
     * @return StorageInfo (id, name, imgUrl, address)
     */
    @GetMapping("")
    public ResponseEntity<MyStorageListResponseDto> myStorageList(Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        List<Storage> storages = storageService.myStorageList(userId);
        return ResponseEntity.ok().body(new MyStorageListResponseDto(storages));
    }

    /**
     * 좌표 및 반경을 기준으로 창고 조회
     *
     * @param longitude 경도
     * @param latitude  위도
     * @param radius    반경
     * @param limit     한 페이지에 나타낼 결과 수
     * @param page      요청 페이지
     */
    @GetMapping("/nearby")
    public ResponseEntity<StorageNearbyListResponseDto> storageNearbyList
    (@RequestParam("lon") double longitude,
     @RequestParam("lat") double latitude,
     @RequestParam("rad") int radius,
     @RequestParam("limit") int limit,
     @RequestParam("page") int page) {
        Point center = geomUtil.createPoint(longitude, latitude);
        int totalCount = storageService.countStorageNearby(center, radius);
        int totalPage = (totalCount - 1) % limit + 1;
        List<StorageNearbyInfo> content = storageService.storageNearby(center, radius, limit, page);
        var response = new StorageNearbyListResponseDto(content, page, content.size(), totalCount, totalPage);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 창고 정보 수정 ( name, descrpition, imgUrl )
     */
    @PatchMapping("/{storageId}")
    public ResponseEntity<Void> modifyStorageInfo(@RequestBody @Valid ModifyStorageInfoRequestDto requestDto,
                                                  @PathVariable Long storageId, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        storageService.modifyStorageInfo(requestDto, storageId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 창고 삭제
     * 창고와 연관된 ItemStorage 삭제 / Item 은 삭제하지 않음
     */
    @DeleteMapping("/{storageId}")
    public ResponseEntity<Void> deleteStorage(@PathVariable Long storageId, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        storageService.deleteStorage(storageId, userId);
        return ResponseEntity.ok().build();
    }
}