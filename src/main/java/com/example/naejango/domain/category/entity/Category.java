package com.example.naejango.domain.category.entity;

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
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Integer id;

    @Column(name = "CATEGORY_NAME")
    private String name;

//    @OneToMany(mappedBy = "category")
//    private List<Item> items = new ArrayList<>();
    // 이쪽 맵핑은 불필요할 수 도 있음
}
