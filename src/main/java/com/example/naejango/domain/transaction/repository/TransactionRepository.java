package com.example.naejango.domain.transaction.repository;

import com.example.naejango.domain.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrTraderId(Long userId, Long traderId);

    @Modifying
    @Query("UPDATE Transaction t SET t.item = null WHERE t.item.id = :itemId")
    void updateItemToNull(@Param("itemId")Long itemId);
}
