package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.FindStorageNearbyRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.response.ItemListResponseDto;
import com.example.naejango.domain.storage.dto.response.MyStorageListResponseDto;
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
     * @return StorageInfoDto (id, name, imgUrl, address)
     */
    @GetMapping("")
    public ResponseEntity<MyStorageListResponseDto> myStorageList(Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        List<Storage> storages = storageService.myStorageList(userId);
        return ResponseEntity.ok().body(new MyStorageListResponseDto(storages));
    }

    /**
     * 창고에 있는 아이템 조회
     */
    @GetMapping("/{storageId}")
    public ResponseEntity<ItemListResponseDto> ItemList(@PathVariable("storageId") Long storageId,
                                                        @RequestParam("status") Boolean status,
                                                        @RequestParam("page") int page,
                                                        @RequestParam("size") int size) {
        List<ItemInfoDto> itemList = storageService.findItemList(storageId, status, page, size);
        return ResponseEntity.ok().body(new ItemListResponseDto(page, size, itemList.size(), itemList));
    }

    /**
     * 좌표 및 반경을 기준으로 창고 조회
     */
    @GetMapping("/nearby")
    public ResponseEntity<StorageNearbyListResponseDto> storageNearbyList
    (@Valid @ModelAttribute FindStorageNearbyRequestDto requestDto) {
        Point center = geomUtil.createPoint(requestDto.getLon(), requestDto.getLat());
        int totalCount = storageService.countStorageNearby(center, requestDto.getRad());
        List<StorageNearbyInfoDto> content = storageService.storageNearby(center, requestDto.getRad(), requestDto.getPage(), requestDto.getSize());
        var response = new StorageNearbyListResponseDto(requestDto.getPage(), requestDto.getSize(), totalCount, content);
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