package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StorageJPQLRepositoryImpl implements StorageJPQLRepository {

    @PersistenceContext
    EntityManager em;

    /**
     * 특정 좌표 및 반경을 기준으로 가까운 창고 조회
     * 거리에 따라 오름차순으로 정렬하고 offset, limit을 인자로 하여 페이징 처리함
     */
    @Override
    public List<StorageNearbyInfoDto> findStorageNearby(Point point, int radius, int page, int size) {
        return em.createQuery("SELECT NEW com.example.naejango.domain.storage.dto.StorageNearbyInfoDto" +
                        "(s, ROUND(CAST(st_distancesphere(:point, s.location) AS double)) AS distance) " +
                        "FROM Storage s WHERE ST_DWithin(:point, s.location, :radius, FALSE) = TRUE ORDER BY distance ASC")
                .setParameter("point", point)
                .setParameter("radius", radius)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
}
