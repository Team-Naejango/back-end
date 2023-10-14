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

    /**
     * 특정 유저의 거래 내역 조회.
     * 거래의 userId, traderId를 조건으로 사용하는데 두 변수 모두 특정 유저의 Id 값으로 받는다.
     * 따라서 유저가 판매자 일 경우, 구매자 일 경우 모두 조회.
     */
    @Query("SELECT t from Transaction t " +
            "left join fetch t.item i " +
            "left join fetch t.user u " +
            "left join fetch u.userProfile up " +
            "left join fetch t.trader tu " +
            "left join fetch tu.userProfile tup " +
            "where t.user.id = :userId or t.trader.id = :traderId")
    List<Transaction> findByUserIdOrTraderId(@Param("userId") Long userId, @Param("traderId") Long traderId);

    /** 특정 거래 id로 조회 */
    @Query("SELECT t from Transaction t " +
            "left join fetch t.item i " +
            "left join fetch t.user u " +
            "left join fetch u.userProfile up " +
            "left join fetch t.trader tu " +
            "left join fetch tu.userProfile tup " +
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
