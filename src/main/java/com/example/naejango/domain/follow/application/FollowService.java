package com.example.naejango.domain.follow.application;

import com.example.naejango.domain.follow.domain.Follow;
import com.example.naejango.domain.follow.dto.response.FindFollowResponseDto;
import com.example.naejango.domain.follow.repository.FollowRepository;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final StorageRepository storageRepository;
    private final EntityManager em;

    /** 팔로우 목록 조회 */
    public List<FindFollowResponseDto> findFollow(Long userId){
        List<Follow> followList = followRepository.findByUserId(userId);

        List<FindFollowResponseDto> findFollowResponseDtoList = new ArrayList<>();
        for(Follow follow: followList){
            findFollowResponseDtoList.add(new FindFollowResponseDto(follow.getStorage()));
        }

        return findFollowResponseDtoList;
    }

    /** 창고 팔로우 등록 */
    @Transactional
    public void addFollow(Long userId, Long storageId){
        // 이미 팔로우 등록 되어 있는지 체크, DB에 존재 하면 true return
        boolean existCheck = followRepository.existsByUserIdAndStorageId(userId, storageId);
        if (existCheck) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXIST);
        }

        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORAGE_NOT_FOUND));

        Follow follow = Follow.builder()
                .id(null)
                .user(em.getReference(User.class, userId))
                .storage(storage)
                .build();

        followRepository.save(follow);
    }


    /** 창고 팔로우 해제 */
    @Transactional
    public void deleteFollow(Long userId, Long storageId){
        Follow follow = followRepository.findByUserIdAndStorageId(userId, storageId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

}
