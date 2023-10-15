package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.dto.StorageAndDistanceDto;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface StorageRepositoryCustom {
    List<StorageAndDistanceDto> findStorageNearby (Point center, int radius, int page, int size);
}
