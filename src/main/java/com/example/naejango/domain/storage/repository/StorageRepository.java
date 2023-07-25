package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    /**
     * 창고를 등록한 유저를 기준으로 조회
     */
    List<Storage> findByUserId(Long userId);

    /**
     * 특정 Point 및 반경을 기준으로 조회
     */
    @Query("SELECT s FROM Storage s WHERE FUNCTION('ST_DWithin', :point, s.location, :radius, false) = true")
    List<Storage> findNearbyStorage(@Param("point") Point point, @Param("radius") int radius);
}