package com.example.naejango.domain.storage.repository;

import com.example.naejango.domain.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    /** 창고를 등록한 유저를 기준으로 조회 */
    List<Storage> findByUserId(Long userId);
}