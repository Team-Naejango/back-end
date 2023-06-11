package com.example.naejango.domain.item.entity;

import com.example.naejango.domain.user.entity.User;
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
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long id;
    private ItemType type;
    @Column(length = 20, nullable = false)
    private String name;
    @Lob
    private String description;
    private Integer viewCount;
    private Boolean status;  // 진행중인 거래 : True, 완료된 거래 : False
    private String link;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;
}
