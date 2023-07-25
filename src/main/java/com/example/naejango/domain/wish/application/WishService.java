package com.example.naejango.domain.wish.application;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.repository.ItemRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.domain.wish.domain.Wish;
import com.example.naejango.domain.wish.dto.response.FindWishResponseDto;
import com.example.naejango.domain.wish.repository.WishRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishService {
    private final WishRepository wishRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /** 관심 목록 조회 */
    public List<FindWishResponseDto> findWish(Long userId){
        List<Wish> wishList = wishRepository.findByUserId(userId);

        List<FindWishResponseDto> findWishResponseDtoList = new ArrayList<>();
        for(Wish wish: wishList){
            findWishResponseDtoList.add(new FindWishResponseDto(wish.getItem()));
        }

        return findWishResponseDtoList;
    }


    /** 아이템 관심 등록 */
    public void addWish(Long userId, Long itemId){
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Wish wish = Wish.builder()
                .id(null)
                .user(user)
                .item(item)
                .build();

        wishRepository.save(wish);
    }


    /** 아이템 관심 해제 */
    public void deleteWish(Long userId, Long itemId){
        Wish wish = wishRepository.findByUserIdAndItemId(userId, itemId);

        wishRepository.delete(wish);
    }

}
