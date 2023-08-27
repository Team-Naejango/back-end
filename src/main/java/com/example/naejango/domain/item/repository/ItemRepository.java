package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.storage.dto.ItemInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT NEW com.example.naejango.domain.storage.dto.ItemInfoDto(i.id, c.name, i.type, i.name, i.imgUrl) " +
            "FROM Item i JOIN i.category c WHERE i.id IN (SELECT itst.item.id FROM ItemStorage itst WHERE itst.storage.id = :storageId) " +
            "AND i.status = :status ORDER BY i.id DESC")
    Page<ItemInfoDto> findByStorageId(@Param("storageId") Long storageId, @Param("status") Boolean status, Pageable pageable);

}
