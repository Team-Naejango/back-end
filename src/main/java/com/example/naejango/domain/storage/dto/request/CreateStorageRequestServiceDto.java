package com.example.naejango.domain.storage.dto.request;


import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStorageRequestServiceDto {
    private String name;
    private String imgUrl;
    private String description;
    private String address;
    private Point location;
}
