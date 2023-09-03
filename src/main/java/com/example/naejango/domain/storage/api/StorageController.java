package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.request.FindStorageNearbyRequestDto;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.storage.dto.response.*;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;
    private final AuthenticationHandler authenticationHandler;
    private final ChannelRepository channelRepository;
    private final GeomUtil geomUtil;

    /**
     * 창고를 생성합니다.
     * @param requestDto 창고 이름(name), 좌표(coord), 주소(address), 설명(description), 이미지링크(imgUrl)
     * @return CreatrStorageResponseDto 창고 Id(storage), 생성 결과(message)
     */
    @PostMapping("")
    public ResponseEntity<CreateStorageResponseDto> createStorage(@RequestBody @Valid CreateStorageRequestDto requestDto, Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
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
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        List<Storage> storages = storageService.myStorageList(userId);
        return ResponseEntity.ok().body(new MyStorageListResponseDto(storages));
    }

    @GetMapping("{storageId}/channel")
    public ResponseEntity<FindStorageChannelResponseDto> findGroupChannel(@PathVariable("storageId") Long storageId) {
        GroupChannel groupChannel = channelRepository.findGroupChannelByStorageId(storageId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        return ResponseEntity.ok().body(new FindStorageChannelResponseDto(new GroupChannelDto(groupChannel), "해당 창고의 그룹 채널 정보가 조회되었습니다."));
    }

    /**
     * 창고에 있는 아이템 조회
     */
    @GetMapping("/{storageId}/items")
    public ResponseEntity<ItemListResponseDto> ItemList(@PathVariable("storageId") Long storageId,
                                                        @RequestParam("status") Boolean status,
                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        List<ItemInfoDto> itemList = storageService.findItemList(storageId, status, page, size);
        if(itemList.size() == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ItemListResponseDto("등록된 아이템이 없습니다.", page, size, itemList));
        return ResponseEntity.ok().body(new ItemListResponseDto("창고 내의 아이템을 조회했습니다.", page, size, itemList));
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
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        storageService.modifyStorageInfo(requestDto, storageId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 창고 삭제
     * 창고와 연관된 ItemStorage 삭제 / Item 은 삭제하지 않음
     */
    @DeleteMapping("/{storageId}")
    public ResponseEntity<Void> deleteStorage(@PathVariable Long storageId, Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        storageService.deleteStorage(storageId, userId);
        return ResponseEntity.ok().build();
    }
}