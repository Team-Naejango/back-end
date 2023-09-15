package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import com.example.naejango.domain.storage.dto.StorageInfoDto;
import com.example.naejango.domain.storage.dto.StorageInfoWithDistanceDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.request.SearchStorageRequestDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;
    private final AuthenticationHandler authenticationHandler;
    private final GeomUtil geomUtil;

    /**
     * 창고 생성
     * @param requestDto 창고 이름(name), 좌표(coord), 주소(address), 설명(description), 이미지링크(imgUrl)
     * @return CreatrStorageResponseDto 창고 Id(storage), 생성 결과(message)
     */
    @PostMapping("")
    public ResponseEntity<CommonResponseDto<Long>> createStorage(@RequestBody @Valid CreateStorageRequestDto requestDto,
                                                           Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        // 창고 생성
        Long storageId = storageService.createStorage(requestDto.getName(), requestDto.getCoord(),
                requestDto.getAddress(), requestDto.getDescription(), requestDto.getImgUrl(), userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CommonResponseDto<>("창고가 생성되었습니다.", storageId)
        );
    }

    /**
     * 내 창고 조회
     * @return StorageInfoDto (id, name, imgUrl, address)
     */
    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<StorageInfoDto>>> myStorageList(Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 창고 조회
        List<StorageInfoDto> storages = storageService.myStorageList(userId);

        return ResponseEntity.ok().body(
                new CommonResponseDto<>("조회 성공", storages)
        );
    }

    /** 근처 창고 검색 */
    @GetMapping("/nearby")
    public ResponseEntity<CommonResponseDto<List<StorageInfoWithDistanceDto>>> storageNearby (@Valid @ModelAttribute SearchStorageRequestDto requestDto) {
        // 검색 조건
        Point center = geomUtil.createPoint(requestDto.getLon(), requestDto.getLat());
        int radius = requestDto.getRad();
        int page = requestDto.getPage();
        int size = requestDto.getSize();

        // 창고 검색
        List<StorageInfoWithDistanceDto> serviceDto = storageService.searchStorage(center, radius, page, size);

        return ResponseEntity.ok().body(new CommonResponseDto<>("검색 완료", serviceDto));

    }

    /** 창고에 등록된 아이템 조회 */
    @GetMapping("/{storageId}/items")
    public ResponseEntity<CommonResponseDto<List<ItemInfoDto>>> itemList(@PathVariable("storageId") Long storageId,
                                                                         @RequestParam("status") boolean status,
                                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        // 카테고리 정보와 함께 아이템 로드
        Page<Item> itemList = storageService.findItemList(storageId, status, page, size);

        // 검색 결과가 없는 경우
        if (itemList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CommonResponseDto<>("등록된 아이템이 없습니다.", new ArrayList<>()));
        }

        // 검색 결과가 있는 경우
        List<ItemInfoDto> result = itemList.getContent().stream().map(ItemInfoDto::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(new CommonResponseDto<>("창고의 아이템 조회 성공", result));
    }

    /**
     * 창고 정보 수정 ( name, descrpition, imgUrl )
     */
    @PatchMapping("/{storageId}")
    public ResponseEntity<CommonResponseDto<Void>> modifyStorageInfo(@RequestBody @Valid ModifyStorageInfoRequestDto requestDto,
                                                  @PathVariable Long storageId, Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        String name = requestDto.getName();
        String imgUrl = requestDto.getImgUrl();
        String description = requestDto.getDescription();

        storageService.modifyStorageInfo(storageId, userId, name, imgUrl, description);
        return ResponseEntity.ok().body(new CommonResponseDto<>("창고 정보 수정 완료", null));
    }

    /**
     * 창고 삭제
     * 창고와 연관된 ItemStorage 삭제 / Item 은 삭제하지 않음
     * 다른 연관된 엔티티 고려 필요
     */
    @DeleteMapping("/{storageId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteStorage(@PathVariable Long storageId, Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        storageService.deleteStorage(storageId, userId);
        return ResponseEntity.ok().body(new CommonResponseDto<>("삭제 완료", null));
    }
}