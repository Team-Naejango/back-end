package com.example.naejango.domain.item.api;

import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.dto.GroupChannelDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.request.SearchItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import com.example.naejango.domain.storage.dto.SearchingConditionDto;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.storage.dto.response.FindStorageChannelResponseDto;
import com.example.naejango.domain.storage.dto.response.SearchItemResponseDto;
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
    private final ChannelRepository channelRepository;
    private final AuthenticationHandler authenticationHandler;
    private final CategoryRepository categoryRepository;
    private final GeomUtil geomUtil;

    /** 아이템 생성 */
    @PostMapping("")
    public ResponseEntity<CreateItemResponseDto> createItem(Authentication authentication,
                                                            @RequestBody CreateItemRequestDto createItemRequestDto) {
        Long userId = authenticationHandler.getUserId(authentication);
        CreateItemResponseDto createItemResponseDto = itemService.createItem(userId, createItemRequestDto);
        return ResponseEntity.created(URI.create("/api/item/"+createItemResponseDto.getId())).body(createItemResponseDto);
    }

    /** 아이템 정보 조회 */
    @GetMapping("/{itemId}")
    public ResponseEntity<FindItemResponseDto> findItem(@PathVariable Long itemId) {
        FindItemResponseDto findItemResponseDto = itemService.findItem(itemId); // N+1 문제 발생
        return ResponseEntity.ok().body(findItemResponseDto);
    }

    /**
     * 아이템 검색
     * 검색 조건은 좌표 / 반경 / 카테고리 / 아이템 타입 / 키워드 / 아이템 상태 입니다.
     * @param requestDto 좌표(lon, lat), 반경(rad), 카테고리(cat), 키워드(keyword), 아이템 타입(type), 아이템 상태(status)
     */
    @GetMapping("/search")
    public ResponseEntity<SearchItemResponseDto> searchStorage(@Valid @ModelAttribute SearchItemRequestDto requestDto) {
        Point center = geomUtil.createPoint(requestDto.getLon(), requestDto.getLat());

        // 카테고리 로드
        Category category = categoryRepository.findById(requestDto.getCatId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 키워드 만들기
        String[] keywords = requestDto.getKeyword() == null?
                new String[]{} : Arrays.stream(requestDto.getKeyword().split(" ")).map(word -> "%" + word + "%").toArray(String[]::new);

        // 조건 생성
        int rad = requestDto.getRad();
        int page = requestDto.getPage();
        int size = requestDto.getSize();
        SearchingConditionDto conditions = SearchingConditionDto.builder()
                .cat(category)
                .itemType(requestDto.getType())
                .keyword(keywords)
                .status(requestDto.getStatus()).build();

        // 검색
        List<SearchItemInfoDto> result = itemService.searchItem(center, rad, page, size, conditions);
        return ResponseEntity.ok().body(new SearchItemResponseDto(new Coord(center), rad, page, size, conditions, result));
    }

    /**
     * 공동구매 아이템에 등록된 그룹 채팅 조회
     * @param itemId 아이템1 id
     * @return FindStorageChannelResponseDto 채널 id(channelId), 결과 메세지(message)
     */
    @GetMapping("/{itemId}/channel")
    public ResponseEntity<FindStorageChannelResponseDto> findGroupChannel(@PathVariable Long itemId) {
        GroupChannel groupChannel = channelRepository.findGroupChannelByItemId(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        return ResponseEntity.ok().body(new FindStorageChannelResponseDto(new GroupChannelDto(groupChannel), "해당 창고의 그룹 채널 정보가 조회되었습니다."));
    }

    /** 아이템 정보 수정 */
    @PatchMapping("/{itemId}")
    public ResponseEntity<ModifyItemResponseDto> modifyItem(Authentication authentication, @PathVariable Long itemId, @RequestBody ModifyItemRequestDto modifyItemRequestDto) {
        Long userId = authenticationHandler.getUserId(authentication);
        ModifyItemResponseDto modifyItemResponseDto = itemService.modifyItem(userId, itemId, modifyItemRequestDto);

        return ResponseEntity.ok().body(modifyItemResponseDto);
    }

}
