package com.example.naejango.domain.transaction.repository;

import com.example.naejango.domain.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t from Transaction t " +
            "left join fetch t.item " +
            "left join fetch UserProfile up on up.id = t.trader.id or up.id = t.user.id " +
            "where t.user.id = :userId or t.trader.id = :traderId")
    List<Transaction> findByUserIdOrTraderId(@Param("userId") Long userId, @Param("traderId") Long traderId);

    /** 특정 거래 id로 조회 */
    @Query("SELECT t from Transaction t " +
            "left join fetch t.item " +
            "left join fetch UserProfile up on up.id = t.trader.id or up.id = t.user.id " +
            "where t.id = :transactionId")
    Optional<Transaction> findByTransactionId(@Param("transactionId") Long transactionId);

    /** 두 유저 사이의 완료 되지 않은 거래 조회 */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId and t.trader.id = :traderId and not t.status = 'TRANSACTION_COMPLETION'")
    List<Transaction> findByUserIdAndTraderId(@Param("userId") Long userId, @Param("traderId") Long traderId);

    @Modifying
    @Query("UPDATE Transaction t SET t.item = null WHERE t.item.id = :itemId")
    void updateItemToNullByItemId(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Transaction t SET t.item = null WHERE t.item.id in :ids")
    void updateItemToNullByItemIdList(@Param("ids") List<Long> itemIdList);
}
