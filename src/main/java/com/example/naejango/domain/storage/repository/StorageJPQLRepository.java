package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.dto.response.StorageNearbyDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageJPQLRepository {
    List<StorageNearbyDto> findStorageNearby(Point point, int radius, int offset, int limit);
}
