package com.example.naejango.domain.item.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.dto.MatchItemsRequestDto;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.SearchingCommandDto;
import com.example.naejango.domain.item.dto.request.*;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.MatchResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/api/item")
@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final AuthenticationHandler authenticationHandler;
    private final GeomUtil geomUtil;

    /** 아이템 생성 */
    @PostMapping("")
    public ResponseEntity<CommonResponseDto<CreateItemResponseDto>> createItem(Authentication authentication,
                                                                               @RequestBody @Valid CreateItemRequestDto createItemRequestDto) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 해시태그에 공백이 있는지, 글자수가 너무 많은지 확인 (공백 이외 많은 예외처리를 할 수 있지만 편의상 공백만 처리함)
        createItemRequestDto.getHashTag().stream().filter(tag -> tag.contains(" ") || tag.length() > 10).findAny().ifPresent(k -> {
            throw new CustomException(ErrorCode.FORGED_REQUEST);
        });

        CreateItemResponseDto createItemResponseDto = itemService.createItem(userId, new CreateItemCommandDto(createItemRequestDto));

        return ResponseEntity.created(URI.create("/api/item/" + createItemResponseDto.getId()))
                .body(new CommonResponseDto<>("아이템 생성 완료", createItemResponseDto));
    }

    /** 아이템 정보 조회 */
    @GetMapping("/{itemId}")
    public ResponseEntity<CommonResponseDto<FindItemResponseDto>> findItem(@PathVariable Long itemId) {
        FindItemResponseDto findItemResponseDto = itemService.findItem(itemId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", findItemResponseDto));
    }

    /**
     * 아이템 검색
     * 검색 조건은 좌표 / 반경 / 카테고리 / 아이템 타입 / 키워드 / 아이템 상태 입니다.
     * @param requestDto 좌표(lon, lat), 반경(rad), 카테고리(cat), 키워드(keyword), 아이템 타입(type), 아이템 상태(status)
     */
    @GetMapping("/search")
    public ResponseEntity<CommonResponseDto<List<SearchItemInfoDto>>> searchStorage(@Valid @ModelAttribute SearchItemRequestDto requestDto) {
        Point center = geomUtil.createPoint(requestDto.getLon(), requestDto.getLat());
        String[] keywords = requestDto.getKeyword() != null ?
                Arrays.stream(requestDto.getKeyword().split(" ")).map(word -> "%" + word + "%").toArray(String[]::new)
                : new String[]{} ;

        // 검색
        List<SearchItemInfoDto> result = itemService.searchItem(new SearchingCommandDto(requestDto, center, keywords));

        return ResponseEntity.ok().body(new CommonResponseDto<>("검색 성공", result));
    }

    /**
     * 아이템 매칭
     * 검색 조건은 좌표 / 반경 / 카테고리 / 아이템 타입 / 키워드 / 아이템 상태 입니다.
     * @param requestDto 좌표(lon, lat), 카테고리(cat), 키워드(keyword), 아이템 타입(type)
     */
    @GetMapping("/match")
    public ResponseEntity<CommonResponseDto<List<MatchResponseDto>>> matchItems(@Valid @ModelAttribute MatchItemsRequestDto requestDto) {
        // 검색
        List<MatchResponseDto> result = itemService.matchItem(requestDto.getRad(), requestDto.getSize(), requestDto.getItemId());

        return ResponseEntity.ok().body(new CommonResponseDto<>("매칭 성공", result));
    }

    /** 아이템 정보 수정 */
    @PatchMapping("/{itemId}")
    public ResponseEntity<CommonResponseDto<ModifyItemResponseDto>> modifyItem(Authentication authentication, @PathVariable Long itemId,
                                                            @RequestBody @Valid ModifyItemRequestDto modifyItemRequestDto) {
        Long userId = authenticationHandler.getUserId(authentication);
        ModifyItemResponseDto modifyItemResponseDto = itemService.modifyItem(userId, itemId, new ModifyItemCommandDto(modifyItemRequestDto));

        return ResponseEntity.ok().body(new CommonResponseDto<>("수정 완료", modifyItemResponseDto));
    }

    /** 아이템 삭제 */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteItem(Authentication authentication, @PathVariable Long itemId) {
        Long userId = authenticationHandler.getUserId(authentication);
        itemService.deleteItem(userId, itemId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("삭제 완료", null));
    }
}
