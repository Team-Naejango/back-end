package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.request.SearchStorageRequestDto;
import com.example.naejango.domain.storage.dto.response.CreateStorageResponseDto;
import com.example.naejango.domain.storage.dto.response.ItemListResponseDto;
import com.example.naejango.domain.storage.dto.response.MyStorageListResponseDto;
import com.example.naejango.domain.storage.dto.response.StorageNearbyListResponseDto;
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
     * 창고를 생성 합니다.
     *
     * @param requestDto 창고 이름(name), 좌표(coord), 주소(address), 설명(description), 이미지링크(imgUrl)
     * @return CreatrStorageResponseDto 창고 Id(storage), 생성 결과(message)
     */
    @PostMapping("")
    public ResponseEntity<CreateStorageResponseDto> createStorage(@RequestBody @Valid CreateStorageRequestDto requestDto,
                                                                  Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        Long storageId = storageService.createStorage(requestDto.getName(), requestDto.getCoord(),
                requestDto.getAddress(), requestDto.getDescription(), requestDto.getImgUrl(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CreateStorageResponseDto(storageId, "창고가 생성되었습니다.")
        );
    }

    /**
     * 나의 창고 리스트 조회
     * @return StorageInfoDto (id, name, imgUrl, address)
     */
    @GetMapping("")
    public ResponseEntity<MyStorageListResponseDto> myStorageList(Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        List<Storage> storages = storageService.myStorageList(userId);
        return ResponseEntity.ok().body(new MyStorageListResponseDto(storages));
    }

    /** 창고 검색 */
    @GetMapping("/nearby")
    public ResponseEntity<StorageNearbyListResponseDto> storageNearby (@Valid @ModelAttribute SearchStorageRequestDto requestDto) {
        Point center = geomUtil.createPoint(requestDto.getLon(), requestDto.getLat());
        int radius = requestDto.getRad();
        int page = requestDto.getPage();
        int size = requestDto.getSize();
        List<StorageNearbyInfoDto> result = storageService.searchStorage(center, radius, page, size);
        return ResponseEntity.ok().body(new StorageNearbyListResponseDto(page, size, result));
    }

    /**
     * 창고에 등록된 아이템 조회
     */
    @GetMapping("/{storageId}/items")
    public ResponseEntity<ItemListResponseDto> ItemList(@PathVariable("storageId") Long storageId,
                                                        @RequestParam("status") boolean status,
                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        // 카테고리 정보와 함께 아이템 로드
        Page<Item> itemList = storageService.findItemList(storageId, status, page, size);

        // 검색 결과가 없는 경우
        if (itemList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ItemListResponseDto("등록된 아이템이 없습니다.", page, size, null));
        }

        // 검색 결과가 있는 경우
        List<ItemInfoDto> result = itemList.getContent().stream().map(ItemInfoDto::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(new ItemListResponseDto("창고 내의 아이템을 조회했습니다.", page, size, result));
    }

    /**
     * 창고 정보 수정 ( name, descrpition, imgUrl )
     */
    @PatchMapping("/{storageId}")
    public ResponseEntity<Void> modifyStorageInfo(@RequestBody @Valid ModifyStorageInfoRequestDto requestDto,
                                                  @PathVariable Long storageId, Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        storageService.modifyStorageInfo(requestDto, storageId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 창고 삭제
     * 창고와 연관된 ItemStorage 삭제 / Item 은 삭제하지 않음
     */
    @DeleteMapping("/{storageId}")
    public ResponseEntity<Void> deleteStorage(@PathVariable Long storageId, Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        storageService.deleteStorage(storageId, userId);
        return ResponseEntity.ok().build();
    }
}