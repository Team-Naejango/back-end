package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.dto.StorageNearbyInfoDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageJPQLRepository {
    List<StorageNearbyInfoDto> findStorageNearby(Point point, int radius, int page, int size);
}
