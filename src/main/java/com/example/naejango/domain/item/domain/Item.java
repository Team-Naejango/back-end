package com.example.naejango.domain.item.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
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

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ItemType type; // BUY, SELL

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ItemDealType dealType; // INDIVIDUAL, GROUP

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private Boolean status;  // 진행중인 거래 : True, 완료된 거래 : False

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public void modifyItem(String name, String description, String imgUrl, ItemType type, ItemDealType dealType, Category category) {
        this.name = name;
        this.description = description;
        this.imgUrl = imgUrl;
        this.type = type;
        this.dealType = dealType;
        this.category = category;
    }
}
