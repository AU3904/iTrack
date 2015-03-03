package com.sports.iTrack.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 行车轨迹对象
 * Created by aaron_lu on 2/4/15.
 */
public class TrackItem extends DataSupport {

    private int sportTpye;
    private int recordPointsCount;
    private long startTime;
    private long endTime;
    private long timestamp;
    private double avgSpeed;
    private double maxSpeed;
    private double minSpeed;
    private double maxAltitude;
    private double minAltitude;
    private double distance;
    private String discription;

    private List<RecordPoint> recordPointList = new ArrayList<RecordPoint>();

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMinSpeed(double minSpeed) {
        this.minSpeed = minSpeed;
    }

    public void setMaxAltitude(double maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public void setMinAltitude(double minAltitude) {
        this.minAltitude = minAltitude;
    }

    public void setSportTpye(int sportTpye) {
        this.sportTpye = sportTpye;
    }

    public void setRecordPointsCount(int recordPointsCount) {
        this.recordPointsCount = recordPointsCount;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<RecordPoint> getRecordPointList() {
//        return DataSupport.where("trackitem_id = ?", String.valueOf(id)).find(RecordPoint.class);
        return recordPointList;
    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }

    public void setRecordPointList(List<RecordPoint> recordPointList) {
        this.recordPointList = recordPointList;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getDistance() {
        return distance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    public double getMinAltitude() {
        return minAltitude;
    }

    public int getSportTpye() {
        return sportTpye;
    }

    public int getRecordPointsCount() {
        return recordPointsCount;
    }

    public String getDiscription() {
        return discription;
    }

    public long getTimestamp() {
        return timestamp;
    }
}