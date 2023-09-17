package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, ItemJPQLRepository {

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT i FROM Item i WHERE i.storage.id = :storageId AND i.status = :status")
    Page<Item> findItemWithCategoryByStorageIdAndStatus(@Param("storageId") Long storageId, @Param("status") boolean status, Pageable pageable);

    @Query("DELETE FROM Item i WHERE i.storage.id = :storageId")
    void deleteByStorageId(@Param("storageId") Long storageId);

    Long findUserIdById(Long itemId);

    @EntityGraph(attributePaths = {"category"})
    Optional<Item> findItemById(Long Id);

    @Query("SELECT i.id FROM Item i WHERE i.storage.id = :storageId")
    List<Long> findItemIdListByStorageId(@Param("storageId") Long storageId);
}
