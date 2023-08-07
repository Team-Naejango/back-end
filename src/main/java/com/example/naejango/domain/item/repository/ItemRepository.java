package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i where i.id in (select itst.item.id from ItemStorage itst where itst.storage.id = :storageId)")
    List<Item> findByStorageId(@Param("storageId") Long storageId);

}
