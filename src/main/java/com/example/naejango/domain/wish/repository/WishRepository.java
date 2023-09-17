package com.example.naejango.domain.wish.repository;

import com.example.naejango.domain.wish.domain.Wish;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {
    @EntityGraph(attributePaths = {"item"})
    List<Wish> findByUserId(Long userId);

    Optional<Wish> findByUserIdAndItemId(Long userId, Long itemId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    @Modifying()
    @Query("delete from Wish w where w.item.id = :id")
    void deleteByItemId(@Param("id") Long itemId);

    @Modifying()
    @Query("delete from Wish w where w.item.id in :ids")
    void deleteByItemIdList(@Param("ids") List<Long> itemIdList);
}
