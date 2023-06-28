package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.ItemStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemStorageRepository extends JpaRepository<ItemStorage, Long> {

}
