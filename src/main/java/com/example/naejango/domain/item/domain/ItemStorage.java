package com.example.naejango.domain.item.domain;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Table(name="item_storage")
public class ItemStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "storage_id")
    private Storage storage;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public ItemStorage(Long id, Storage storage, Item item) {
        this.id = id;
        this.storage = storage;
        this.item = item;
    }
}
