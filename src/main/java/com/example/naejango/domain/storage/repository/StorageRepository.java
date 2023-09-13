package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.StorageAndDistanceDto;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    /* 회원 id를 기준으로 조회 */
    @Query("select s from Storage s where s.user.id = :userId ORDER BY s.createdDate DESC")
    List<Storage> findByUserId(@Param("userId") Long userId);

    /* 특정 좌표 및 반경 내에 있는 모든 창고 조회 */
    @Query("select NEW com.example.naejango.domain.storage.dto.StorageAndDistanceDto(s, ROUND(CAST(ST_DistanceSphere(:center, s.location) AS double)) AS distance) " +
            "from Storage s where St_DWithin(:center, s.location, :radius, false) = true ORDER BY distance ASC ")
    List<StorageAndDistanceDto> findStorageNearby (@Param("center") Point center, @Param("radius") int radius, Pageable pageable);

    @Query("select s.user.id from Storage s where s.id = :storageId")
    Long findUserIdByStorageId(@Param("storageId") Long StorageId);
}