package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long>, StorageRepositoryCustom {

    /* 회원 id를 기준으로 조회 */
    @Query("select s from Storage s where s.user.id = :userId ORDER BY s.createdDate DESC")
    List<Storage> findByUserId(@Param("userId") Long userId);

    /* 창고 id로 회원 id 조회 */
    @Query("select s.user.id from Storage s where s.id = :storageId")
    Long findUserIdByStorageId(@Param("storageId") Long StorageId);

    /* 창고 id와 회원 id로 창고 조회 */
    Optional<Storage> findByIdAndUserId(Long id, Long userId);
}