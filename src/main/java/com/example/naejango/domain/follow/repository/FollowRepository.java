package com.example.naejango.domain.follow.repository;

import com.example.naejango.domain.follow.domain.Follow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    @EntityGraph(attributePaths = {"storage"})
    List<Follow> findByUserId(Long userId);

    Optional<Follow> findByUserIdAndStorageId(Long userId, Long storageId);

    boolean existsByUserIdAndStorageId(Long userId, Long storageId);

    @Modifying
    @Query("delete from Follow f where f.storage.id = :id")
    void deleteByStorageId(@Param("id") Long storageId);

    @Modifying
    @Query("delete from Follow f where f.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
