package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.application.SearchingConditionDto;
import com.example.naejango.domain.storage.dto.SearchStorageResultDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageJPQLRepository {
    List<SearchStorageResultDto> searchStorageByConditions(Point center, int radius, int page, int size, SearchingConditionDto conditionDto);
}
