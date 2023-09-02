package com.example.naejango.global.common.util;

import com.example.naejango.domain.storage.dto.Coord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class GeomUtil {
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final GeometryFactory factory = new GeometryFactory();

    public Point createPoint(double longitude, double latitude) {
        return factory.createPoint(new Coordinate(longitude, latitude));
    }

    public Point createPoint(Coord coord) {
        return factory.createPoint(new Coordinate(coord.getLongitude(), coord.getLatitude()));
    }

    public int calculateDistance(Point point1, Point point2) {
        return (int) Math.round(calculateDistance(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
    }

    public double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        double lon1Rad = Math.toRadians(lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lon2Rad = Math.toRadians(lon2);
        double lat2Rad = Math.toRadians(lat2);

        // Haversine 공식
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

}