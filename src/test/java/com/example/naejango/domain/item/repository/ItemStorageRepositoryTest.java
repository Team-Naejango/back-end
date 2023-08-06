package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemStorage;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.repository.StorageRepository;
import com.example.naejango.global.common.handler.GeomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemStorageRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ItemStorageRepository itemStorageRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    StorageRepository storageRepository;

    GeomUtil geomUtil = new GeomUtil();

    @Test
    @DisplayName("")
    void deleteByStorageIdTest(){
        // given
        Storage storage1 = Storage.builder().name("test1").description("").address("").imgUrl("").location(geomUtil.createPoint(1, 1)).build();
        Storage storage2 = Storage.builder().name("test2").description("").address("").imgUrl("").location(geomUtil.createPoint(1, 1)).build();
        Item item1 = Item.builder().status(true).type(ItemType.BUY).name("item1").imgUrl("").viewCount(0).description("").build();
        Item item2 = Item.builder().status(true).type(ItemType.BUY).name("item1").imgUrl("").viewCount(0).description("").build();
        storageRepository.save(storage1);
        storageRepository.save(storage2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        ItemStorage is1 = ItemStorage.builder().item(item1).storage(storage1).build();
        ItemStorage is2 = ItemStorage.builder().item(item1).storage(storage2).build();
        ItemStorage is3 = ItemStorage.builder().item(item2).storage(storage1).build();
        itemStorageRepository.save(is1);
        itemStorageRepository.save(is2);
        itemStorageRepository.save(is3);
        em.flush(); em.clear();

        // when
        itemStorageRepository.deleteByStorageId(storage1.getId());
        
        // then
        Storage result1 = storageRepository.findById(storage1.getId()).orElseGet(()->Storage.builder().name("실패").build());
        Storage result2 = storageRepository.findById(storage2.getId()).orElseGet(()->Storage.builder().name("실패").build());
        List<Item> result3 = itemRepository.findByStorageId(storage1.getId());
        List<Item> result4 = itemRepository.findByStorageId(storage2.getId());
        Assertions.assertEquals(0, result1.getItemStorages().size()); // 참고) 추가 쿼리 발생
        Assertions.assertEquals(result2.getItemStorages().size(), 1); // 참고) 추가 쿼리 발생
        Assertions.assertEquals(result3.size(), 0);
        Assertions.assertEquals(result4.size(), 1);
    }

}