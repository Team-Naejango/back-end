package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.dto.MatchingConditionDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.storage.dto.SearchingConditionDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemJPQLRepository {
    List<SearchItemsDto> findItemsByConditions(Point center, int radius, int page, int size, Category cat, SearchingConditionDto conditionDto);
    List<MatchItemDto> findMatchByCondition(Point center, int radius, int size, MatchingConditionDto condition);


}
