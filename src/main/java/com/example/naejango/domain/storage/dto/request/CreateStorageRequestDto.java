package com.example.naejango.domain.storage.dto.request;

import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStorageRequestDto {
    private String name;
    private String imgUrl;
    private String description;
    private String address;
    private double longitude;
    private double latitude;

    public CreateStorageRequestServiceDto toServiceDto(Point location) {
        return new CreateStorageRequestServiceDto(name, imgUrl, description, address, location);
    }
}