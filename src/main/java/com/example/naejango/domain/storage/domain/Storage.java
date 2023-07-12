package com.example.naejango.domain.storage.domain;

import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.user.domain.User;
import lombok.*;

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
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String imgUrl;

    @Column
    private String description;

    @Column(nullable = false)
    private String address;
    /*
     Hibernate-spatial 라이브러리를 이용하여
     DB의 Geometry 데이터와 객체 맵핑을 하려고 했으나
     "Cannot get geometry object from data you send to the GEOMETRY field"
     라는 오류가 계속 발생함
     네이티브 쿼리를 이용하여 데이터를 넣을 수 있으나
     다시 데이터를 객체에 맵핑하는 것도 복잡해지기 때문에
     그냥 double 형태로 위도와 경도를 넣음
     */
    @Embedded
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
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
        this.location = new Location(requestDto.getLatitude(), requestDto.getLongitude());
    }
}