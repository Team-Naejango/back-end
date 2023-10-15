package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.MatchItemDto;
import com.example.naejango.domain.item.dto.MatchingConditionDto;
import com.example.naejango.domain.item.dto.SearchItemsDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.naejango.domain.item.domain.QCategory.category;
import static com.example.naejango.domain.item.domain.QItem.item;
import static com.example.naejango.domain.storage.domain.QStorage.storage;
import static com.example.naejango.domain.user.domain.QUser.user;

@Repository
public class ItemRepositoryImpl implements ItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public ItemRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SearchItemsDto> findItemsByConditions(Point center, int radius, int page, int size, Category cat,
                                                      String[] keywords, ItemType itemType, Boolean status) {

        return queryFactory.select(Projections.constructor(SearchItemsDto.class,
                        item,
                        category,
                        storage,
                        Expressions.numberTemplate(Integer.class, "ROUND(CAST(ST_DistanceSphere({0}, {1}) AS double))", center, storage.location).as("distance"))
                )
                .from(storage)
                .join(storage.items, item)
                .join(item.category, category)
                .where(
                        catEq(cat),
                        itemTypeEq(itemType),
                        nameLikeAnd(keywords),
                        distanceWithin(center, radius),
                        statusEq(status)
                )
                .offset((long) page * size)
                .limit(size)
                .orderBy(Expressions.numberPath(Integer.class,"distance").asc())
                .fetch();
    }

    @Override
    public List<MatchItemDto> findMatchByCondition(Point center, int radius, int size, MatchingConditionDto condition) {
        return queryFactory.select(Projections.constructor(MatchItemDto.class,
                        item,
                        category,
                        user,
                        Expressions.numberTemplate(Integer.class, "ROUND(CAST(ST_DistanceSphere({0}, {1}) AS double))", center, storage.location).as("distance"))
                )
                .from(storage)
                .join(storage.items, item)
                .join(storage.user, user)
                .join(item.category, category)
                .where(
                        distanceWithin(center, radius),
                        catEq(condition.getCategory()),
                        itemTypeIn(condition.getItemTypes()),
                        nameLikeOr(condition.getHashTags())
                )
                .limit(size)
                .orderBy(Expressions.numberPath(Integer.class, "distance").asc())
                .fetch();
    }

    private BooleanExpression distanceWithin(Point center, int radius) {
        return Expressions.booleanTemplate("ST_DWithin({0}, {1}, {2}, {3})", center, storage.location, radius, false).eq(true);
    }

    private BooleanBuilder nameLikeOr(String[] words) {
        BooleanBuilder condition = new BooleanBuilder();
        for (String tag : words) {
            condition.or(item.name.like(tag));
        }
        return condition;
    }

    private BooleanBuilder nameLikeAnd(String[] words) {
        BooleanBuilder condition = new BooleanBuilder();
        for (String tag : words) {
            condition.and(item.name.like(tag));
        }
        return condition;
    }

    private static BooleanExpression itemTypeIn(ItemType[] itemTypes) {
        return itemTypes != null? item.itemType.in(itemTypes) : null;
    }
    private Predicate itemTypeEq(ItemType itemType) {
        return itemType != null? item.itemType.eq(itemType) : null;
    }

    private BooleanExpression catEq(Category cat) {
        return cat != null? category.eq(cat) : null;
    }

    private BooleanExpression statusEq(Boolean status) {
        return status != null? item.status.eq(status) : null;
    }

}
