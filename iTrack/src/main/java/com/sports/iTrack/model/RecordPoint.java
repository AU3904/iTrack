package com.sports.iTrack.model;

import org.litepal.crud.DataSupport;

/**
 * 单个采样点
 * Created by aaron_lu on 2/4/15.
 */
public class RecordPoint extends DataSupport {

    private float speed;
    private long timestamp;
    private double altitude;
    private double latitude;
    private double longitude;
    private double distance;
    private TrackItem trackItem;

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getDistance() {
        return distance;
    }

    public float getSpeed() {
        return speed;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TrackItem getTrackItem() {
        return trackItem;
    }

    public void setTrackItem(TrackItem trackItem) {
        this.trackItem = trackItem;
    }
}
