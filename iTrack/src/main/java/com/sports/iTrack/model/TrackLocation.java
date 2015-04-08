package com.sports.iTrack.model;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

/**
 * 单例模式，用来传递CoreService 与 MainActivity 直接的数据
 */
public class TrackLocation {
    private static TrackLocation ourInstance = new TrackLocation();

    public static TrackLocation getInstance() {
        return ourInstance;
    }

    private TrackLocation() {
    }

    public BDLocation getBdLocation() {
        return bdLocation;
    }

    public void setBdLocation(BDLocation bdLocation) {
        this.bdLocation = bdLocation;
    }

    public ArrayList<LatLng> getBdLocationArrayList() {
        return latLngArrayList;
    }

    public void setBdLocationArrayList(ArrayList<LatLng> latLngArray) {
        this.latLngArrayList = latLngArray;
    }

    private BDLocation bdLocation;
    private ArrayList<LatLng> latLngArrayList;
}
