package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.application.SearchingConditionDto;
import com.example.naejango.domain.storage.dto.SearchStorageResultDto;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StorageJPQLRepositoryImpl implements StorageJPQLRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<SearchStorageResultDto> searchStorageByConditions(Point center, int radius, int page, int size, SearchingConditionDto conditionDto) {
        var query = em.createQuery(searchingQueryBuilder(conditionDto), SearchStorageResultDto.class);
        query.setParameter("center", center);
        query.setParameter("radius", radius);
        if (conditionDto.getCat() != null) query.setParameter("cat", conditionDto.getCat());
        if (conditionDto.getItemType() != null) query.setParameter("type", conditionDto.getItemType());
        for (int i = 1; i <= conditionDto.getKeyword().length; i++) {
            query.setParameter("keyword" + i, conditionDto.getKeyword()[i - 1]);
        }
        if (conditionDto.getStatus() != null) query.setParameter("status", conditionDto.getStatus());
        return query.setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    private static String searchingQueryBuilder (SearchingConditionDto conditions) {
        String SELECT = "SELECT NEW com.example.naejango.domain.storage.dto.SearchStorageResultDto";
        String PROJECTION = "(st, ROUND(CAST(ST_DistanceSphere(:center, st.location) AS double)) AS distance) ";
        String FROM = "FROM Storage st ";
        String JOIN_ITEM = "JOIN st.itemStorages itst JOIN itst.item it ";
        String JOIN_ITEM_CAT = "JOIN st.itemStorages itst JOIN itst.item it JOIN it.category c ";
        String WHERE_DISTANCE_CONDITION = "WHERE ST_DWithin(:center, st.location, :radius, FALSE) = TRUE ";
        String AND_CAT = "AND c = :cat ";
        String AND_KEYWORD = "AND it.name LIKE :keyword";
        String AND_TYPE = "AND it.type = :type ";
        String AND_STATUS = "AND it.status = :status ";
        String ORDER_DISTANCE = "ORDER BY distance ASC";
        Category cat = conditions.getCat();
        ItemType type = conditions.getItemType();
        String[] keywords = conditions.getKeyword();
        Boolean status = conditions.getStatus();

        StringBuilder sb = new StringBuilder();

        sb.append(SELECT);
        sb.append(PROJECTION);
        sb.append(FROM);
        // JOIN
        if(cat != null){
            sb.append(JOIN_ITEM_CAT);
        } else if (type != null || keywords != null || status != null) {
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
