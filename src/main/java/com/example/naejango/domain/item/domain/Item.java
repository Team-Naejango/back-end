package com.example.naejango.domain.item.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import com.example.naejango.domain.item.dto.MatchingConditionDto;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="item")
public class Item extends TimeAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(length = 30, nullable = false)
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
    private String tag;

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

    public void modifyItem(String name, String description, String imgUrl, Category category) {
        this.name = name;
        this.description = description;
        this.imgUrl = imgUrl;
        this.category = category;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public MatchingConditionDto getMatchingCondition() {
        return MatchingConditionDto.builder()
                .category(category)
                .hashTags(Arrays.stream(tag.split(" ")).map(word -> "%" + word + "%").toArray(String[]::new))
                .itemTypes(
                        itemType.equals(ItemType.INDIVIDUAL_SELL) ? new ItemType[]{ItemType.INDIVIDUAL_BUY} :
                                itemType.equals(ItemType.GROUP_BUY) ? new ItemType[]{ItemType.GROUP_BUY, ItemType.INDIVIDUAL_BUY} :
                                        new ItemType[]{ItemType.GROUP_BUY, ItemType.INDIVIDUAL_SELL}
                        // 개인 판매 : 개인 구매
                        // 공동 구매 : 공동 구매, 개인 구매
                        // 개인 구매 : 공동 구매, 개인 판매
                )
                .build();
    }
}
