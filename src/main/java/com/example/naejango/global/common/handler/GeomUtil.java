package com.example.naejango.global.common.handler;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class GeomUtil {
    private final GeometryFactory factory = new GeometryFactory();

    public Point createPoint(double latitude, double longitude) {
        return factory.createPoint(new Coordinate(latitude, longitude));
    }
}