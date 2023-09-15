package com.example.naejango.domain.wish.repository;

import com.example.naejango.domain.wish.domain.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {
    List<Wish> findByUserId(Long userId);

    Optional<Wish> findByUserIdAndItemId(Long userId, Long itemId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
