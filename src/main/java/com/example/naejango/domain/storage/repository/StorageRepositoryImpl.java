package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.dto.StorageAndDistanceDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.naejango.domain.storage.domain.QStorage.storage;

@Repository
public class StorageRepositoryImpl implements StorageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public StorageRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<StorageAndDistanceDto> findStorageNearby(Point center, int radius, int page, int size) {
        return queryFactory
                .select(Projections.constructor(StorageAndDistanceDto.class,
                        storage,
                        Expressions.numberTemplate(Integer.class, "ROUND(CAST(ST_DistanceSphere({0}, {1}) AS double))", center, storage.location).as("distance")))
                .from(storage)
                .where(
                        distanceWithin(center, radius)
                )
                .limit(size)
                .orderBy(Expressions.numberPath(Integer.class, "distance").asc())
                .fetch();
    }

    private BooleanExpression distanceWithin(Point center,  int radius) {
        return Expressions.booleanTemplate("ST_DWithin({0}, {1}, {2}, {3})", center, storage.location, radius, false).eq(true);
    }
}
