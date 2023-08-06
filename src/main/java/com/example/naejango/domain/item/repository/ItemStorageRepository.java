package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.ItemStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemStorageRepository extends JpaRepository<ItemStorage, Long> {

    @Modifying
    @Query("delete from ItemStorage i where i.storage.id = :storageId")
    int deleteByStorageId(@Param("storageId") Long storageId);

}
