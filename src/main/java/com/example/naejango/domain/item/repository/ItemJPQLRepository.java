package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.storage.application.SearchingConditionDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemJPQLRepository {
    List<SearchItemsDto> findItemsByConditions(Point center, int radius, int page, int size, SearchingConditionDto conditionDto);

}