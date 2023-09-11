package com.example.naejango.domain.storage.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.storage.dto.request.ModifyStorageInfoRequestDto;
import com.example.naejango.domain.user.domain.User;
import lombok.*;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "storage")
public class Storage extends TimeAuditingEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String imgUrl;

    @Column
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(columnDefinition = "Geometry(Point, 4326)", nullable = false)
    private Point location;

    @OneToMany(mappedBy = "storage")
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void modify(ModifyStorageInfoRequestDto requestDto) {
        this.description = requestDto.getDescription();
        this.name = requestDto.getName();
        this.imgUrl = requestDto.getImgUrl();
    }

}