package com.example.naejango.domain.storage.domain;

import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.user.domain.User;
import lombok.*;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "storage")
public class Storage implements Serializable {

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

    @Column(columnDefinition = "POINT")
    private Point location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "storage")
    List<StorageItem> storageItems;

    public void toBeNamedMethod(User user) {
        this.user = user;
        user.allocateStorage(this);
    }

    public Storage(CreateStorageRequestDto requestDto) {
        this.name = requestDto.getName();
        this.imgUrl = requestDto.getImgUrl();
        this.description = requestDto.getDescription();
        this.address = requestDto.getAddress();
    }
}