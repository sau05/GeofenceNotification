package com.saurabh.geofence_notification.model;

/**
 * Created by kiris on 3/5/2018.
 */

public class GeoItem {
    private double latitude,longitude,radius;
    private boolean in,out;

//    public GeoItem(double latitude,double longitude,double radius,boolean in,boolean out){
//        this.latitude=latitude;
//        this.longitude=longitude;
//        this.radius=radius;
//        this.in=in;
//        this.out=out;
//    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }
}
