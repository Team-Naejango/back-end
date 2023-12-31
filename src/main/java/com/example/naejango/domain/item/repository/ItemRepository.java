package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT i FROM Item i WHERE i.storage.id = :storageId AND i.status = :status")
    Page<Item> findItemWithCategoryByStorageIdAndStatus(@Param("storageId") Long storageId, @Param("status") boolean status, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Optional<Item> findItemWithCategoryById(Long Id);

    @EntityGraph(attributePaths = {"storage"})
    Optional<Item> findItemWithStorageById(Long Id);

    @Query("SELECT i.id FROM Item i WHERE i.storage.id = :storageId")
    List<Long> findItemIdListByStorageId(@Param("storageId") Long storageId);

    @Modifying
    @Query("UPDATE Item i SET i.status = false WHERE i.id = :itemId")
    void updateItemStatusToFalse(@Param("itemId") Long itemId);

}
