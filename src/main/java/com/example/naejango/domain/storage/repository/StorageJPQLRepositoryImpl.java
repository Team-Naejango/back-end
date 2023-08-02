package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.dto.response.StorageNearbyDto;
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
    public List<StorageNearbyDto> findStorageNearby(Point point, int radius, int offset, int limit) {
        return em.createQuery("select new com.example.naejango.domain.storage.dto.response.StorageNearbyDto" +
                        "(s, round(cast(ST_DistanceSphere(:point, s.location) as double)) as distance) " +
                        "from Storage s where ST_DWithin(:point, s.location, :radius, false)=true order by distance asc")
                .setParameter("point", point)
                .setParameter("radius", radius)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
