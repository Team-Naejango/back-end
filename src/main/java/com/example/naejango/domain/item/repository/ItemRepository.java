package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.id IN (SELECT itst.item.id FROM ItemStorage itst WHERE itst.storage.id = :storageId) " +
            "AND i.status = :status ORDER BY i.id DESC")
    Page<Item> findByStorageId(@Param("storageId") Long storageId, @Param("status") Boolean status, Pageable pageable);
}
