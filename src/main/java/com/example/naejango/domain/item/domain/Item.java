package com.example.naejango.domain.item.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="item")
public class Item extends TimeAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String imgUrl;

    @Column(nullable = false, name = "item_type")
    @Enumerated(value = EnumType.STRING)
    private ItemType itemType; // INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private Boolean status;  // 진행중인 거래 : True, 완료된 거래 : False

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;

    public void modifyItem(String name, String description, String imgUrl, ItemType type, Category category) {
        this.name = name;
        this.description = description;
        this.imgUrl = imgUrl;
        this.itemType = type;
        this.category = category;
    }

    public void putItem(Storage storage) {
        this.storage = storage;
    }
}
