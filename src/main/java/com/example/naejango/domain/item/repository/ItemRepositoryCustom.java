package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.MatchItemDto;
import com.example.naejango.domain.item.dto.MatchingConditionDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface ItemRepositoryCustom {

    List<SearchItemsDto> findItemsByConditions(Point center, int radius, int page, int size, Category cat, String[] keywords, ItemType itemType, Boolean status);

    List<MatchItemDto> findMatchByCondition(Point center, int radius, int size, MatchingConditionDto condition);

}
