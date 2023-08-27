package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long>, StorageJPQLRepository {
    /**
     * 회원 id를 기준으로 조회
     */
    @Query("select s from Storage s where s.user.id = :userId ORDER BY s.createdDate ASC")
    List<Storage> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 좌표 및 반경 내에 있는 모든 창고의 개수 조회
     */
    @Query("select count(s) from Storage s where St_DWithin(:center, s.location, :radius, false) = true")
    int countStorageWithinRadius(@Param("center") Point center, @Param("radius") int radius);

    /**
     * 특정 좌표 및 반경 내에 있는 모든 창고 조회
     */
    @Query(value = "select s from Storage s where St_DWithin(:center, s.location, :radius, false) = true")
    List<Storage> findStorageWithinRadius (@Param("center") Point center, @Param("radius") int radius);
}