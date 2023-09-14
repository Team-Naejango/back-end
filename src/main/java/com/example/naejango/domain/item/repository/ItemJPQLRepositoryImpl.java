package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.example.naejango.domain.storage.dto.SearchingConditionDto;
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

    private static String searchingQueryBuilder (SearchingConditionDto conditions, Category cat) {
        String SELECT = "SELECT NEW com.example.naejango.domain.storage.dto.SearchItemsDto";
        String PROJECTION = "(it, st, c, ROUND(CAST(ST_DistanceSphere(:center, st.location) AS double)) AS distance) ";
        String FROM = "FROM Storage st ";
        String JOIN_ITEM_CAT = "JOIN st.items it JOIN it.category c ";
        String JOIN_ITEM = "JOIN st.items it ";
        String WHERE_DISTANCE_CONDITION = "WHERE ST_DWithin(:center, st.location, :radius, FALSE) = TRUE ";
        String AND_CAT = "AND c = :cat ";
        String AND_KEYWORD = "AND it.name LIKE :keyword";
        String AND_TYPE = "AND it.itemType = :itemType ";
        String AND_STATUS = "AND it.status = :status ";
        String ORDER_DISTANCE = "ORDER BY distance ASC";

        ItemType type = conditions.getItemType();
        String[] keywords = conditions.getKeyword();
        Boolean status = conditions.getStatus();

        StringBuilder sb = new StringBuilder();
        // SELECT
        sb.append(SELECT);
        sb.append(PROJECTION);
        // FROM
        sb.append(FROM);
        // JOIN
        if(cat != null){
            sb.append(JOIN_ITEM_CAT);
        } else if (!(type == null && keywords.length == 0 && status == null)) {
            sb.append(JOIN_ITEM);
        }
        // WHERE
        sb.append(WHERE_DISTANCE_CONDITION);
        if (cat != null) sb.append(AND_CAT);
        if (type != null) sb.append(AND_TYPE);
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
