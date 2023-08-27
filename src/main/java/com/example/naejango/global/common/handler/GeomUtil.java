package com.example.naejango.global.common.handler;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class GeomUtil {
    private static final double EARTH_RADIUS_METERS = 6371000.0;
    private final Random random = new Random();

    private final GeometryFactory factory = new GeometryFactory();

    public Point createPoint(double longitude, double latitude) {
        return factory.createPoint(new Coordinate(longitude, latitude));
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

    public Point getRandomPointInGangnam() {
        final double MIN_LATITUDE = 37.473824;
        final double MAX_LATITUDE = 37.517071;
        final double MIN_LONGITUDE = 127.014418;
        final double MAX_LONGITUDE = 127.060426;

        double randomLatitude = MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * random.nextDouble();
        double randomLongitude = MIN_LONGITUDE + (MAX_LONGITUDE - MIN_LONGITUDE) * random.nextDouble();
        return createPoint(randomLongitude, randomLatitude);
    }

}