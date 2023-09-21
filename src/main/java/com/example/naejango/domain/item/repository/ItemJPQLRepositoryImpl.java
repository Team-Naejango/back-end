package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.MatchingConditionDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.item.dto.SearchingConditionDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * 검색 쿼리를 동적으로 생성하기 위한 Repository 입니다.
 */
@Repository
public class ItemJPQLRepositoryImpl implements ItemJPQLRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<SearchItemsDto> findItemsByConditions(Point center, int radius, int page, int size, Category cat, SearchingConditionDto conditionDto) {
        var query = em.createQuery(searchingQueryBuilder(conditionDto, cat), SearchItemsDto.class);

        // 파라미터를 지정합니다.
        query.setParameter("center", center);
        query.setParameter("radius", radius);
        if (cat != null) query.setParameter("cat", cat);
        if (conditionDto.getItemType() != null) query.setParameter("itemType", conditionDto.getItemType());
        for (int i = 1; i <= conditionDto.getKeyword().length; i++) {
            query.setParameter("keyword" + i, conditionDto.getKeyword()[i - 1]);
        }
        if (conditionDto.getStatus() != null) query.setParameter("status", conditionDto.getStatus());
        return query.setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }


    @Override
    public List<MatchItemDto> findMatchByCondition(Point center, int radius, int size, MatchingConditionDto condition) {
        int tagCount = condition.getHashTags().length;
        var query = em.createQuery(matchingQueryBuilder(tagCount), MatchItemDto.class);
        query.setParameter("center", center);
        query.setParameter("radius", radius);
        query.setParameter("cat", condition.getCategory());
        query.setParameter("itemType", condition.getItemTypes());
        if (tagCount == 1) {
            query.setParameter("tag1", condition.getHashTags()[0]);
        } else if(tagCount == 2) {
            query.setParameter("tag1", condition.getHashTags()[0]);
            query.setParameter("tag2", condition.getHashTags()[1]);
        } else {
            query.setParameter("tag1", condition.getHashTags()[0]);
            query.setParameter("tag2", condition.getHashTags()[1]);
            query.setParameter("tag3", condition.getHashTags()[2]);
        }
        return query
                .setMaxResults(size)
                .getResultList();
    }

    private static String matchingQueryBuilder(int tagCount) {
        String SELECT = "SELECT NEW com.example.naejango.domain.item.dto.MatchItemDto";
        String PROJECTION = "(it, c, ROUND(CAST(ST_DistanceSphere(:center, st.location) AS double)) AS distance) ";
        String FROM = "FROM Storage st JOIN st.items it JOIN it.category c ";
        String WHERE_DISTANCE_CONDITION = "WHERE ST_DWithin(:center, st.location, :radius, FALSE) = TRUE ";
        String AND_CAT_CONDITION = "AND c = :cat ";
        String AND_TYPE = "AND it.itemType IN :itemType ";
        String AND_STATUS_TRUE = "AND it.status = true ";
        String ORDER_DISTANCE = "ORDER BY distance ASC";

        StringBuilder sb = new StringBuilder();
        sb.append(SELECT);
        sb.append(PROJECTION);
        sb.append(FROM);
        sb.append(WHERE_DISTANCE_CONDITION);
        sb.append(AND_CAT_CONDITION);
        if (tagCount == 1) {
            sb.append("AND it.name = :tag1 ");
        } else if(tagCount == 2) {
            sb.append("AND (it.name = :tag1 OR it.name = :tag2) ");
        } else {
            sb.append("AND (it.name = :tag1 OR it.name = :tag2 OR it.name = :tag3) ");
        }
        sb.append(AND_TYPE);
        sb.append(AND_STATUS_TRUE);
        sb.append(ORDER_DISTANCE);
        return sb.toString();
    }

    private static String searchingQueryBuilder(SearchingConditionDto conditions, Category cat) {
        String SELECT = "SELECT NEW com.example.naejango.domain.item.dto.SearchItemsDto";
        String PROJECTION = "(it, st, c, ROUND(CAST(ST_DistanceSphere(:center, st.location) AS double)) AS distance) ";
        String FROM_STORAGE_JOIN_ITEM_CAT = "FROM Storage st JOIN st.items it JOIN it.category c ";
        String WHERE_DISTANCE_CONDITION = "WHERE ST_DWithin(:center, st.location, :radius, FALSE) = TRUE ";
        String AND_CAT = "AND c = :cat ";
        String AND_KEYWORD = "AND it.name LIKE :keyword";
        String AND_TYPE = "AND it.itemType IN :itemType ";
        String AND_STATUS = "AND it.status = :status ";
        String ORDER_DISTANCE = "ORDER BY distance ASC";

        ItemType[] itemType = conditions.getItemType();
        String[] keywords = conditions.getKeyword();
        Boolean status = conditions.getStatus();

        StringBuilder sb = new StringBuilder();
        // SELECT
        sb.append(SELECT);
        sb.append(PROJECTION);
        // FROM
        sb.append(FROM_STORAGE_JOIN_ITEM_CAT);
        // WHERE
        sb.append(WHERE_DISTANCE_CONDITION);
        if (cat != null) sb.append(AND_CAT);
        if (itemType != null) sb.append(AND_TYPE);
        for (int i = 1; i <= keywords.length; i++) {
            sb.append(AND_KEYWORD);
            sb.append(i);
            sb.append(" ");
        }
        if (status != null) sb.append(AND_STATUS);

        // ORDER BY
        sb.append(ORDER_DISTANCE);

        return sb.toString();
    }
}
