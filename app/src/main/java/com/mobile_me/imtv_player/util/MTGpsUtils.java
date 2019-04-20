package com.mobile_me.imtv_player.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile_me.imtv_player.model.MTGpsPoint;

import java.io.IOException;
import java.util.List;

public class MTGpsUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isPointInPolygon(
            MTGpsPoint point,
            MTGpsPoint[] polygon)
    {
        if (polygon == null) return false;
        int i;
        double angle=0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;
        int n = polygon.length;

        for (i=0;i<n;i++) {
            point1_lat = polygon[i].getLatitude() - point.getLatitude();
            point1_long = polygon[i].getLongitude() - point.getLongitude();
            point2_lat = polygon[(i+1)%n].getLatitude() - point.getLatitude();
            //you should have paid more attention in high school geometry.
            point2_long = polygon[(i+1)%n].getLongitude() - point.getLongitude();
            angle += Angle2D(point1_lat,point1_long,point2_lat,point2_long);
        }

        if (Math.abs(angle) < Math.PI)
            return false;
        else
            return true;
    }

    private static double Angle2D(double y1, double x1, double y2, double x2)
    {
        double dtheta,theta1,theta2;

        theta1 = Math.atan2(y1,x1);
        theta2 = Math.atan2(y2,x2);
        dtheta = theta2 - theta1;
        while (dtheta > Math.PI)
            dtheta -= 2*Math.PI;
        while (dtheta < -Math.PI)
            dtheta += 2*Math.PI;

        return(dtheta);
    }

    public static MTGpsPoint[] parsePolygon(String polygonS)  {
        if (polygonS == null) return null;
        try {
            return objectMapper.readValue(polygonS, new TypeReference<MTGpsPoint[]>() {});
        } catch (Exception e) {
        }
        return null;
    }

    public static String writePolygonAsString(MTGpsPoint[] polygon)  {
        if (polygon == null) return null;
        try {
            return objectMapper.writeValueAsString(polygon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
