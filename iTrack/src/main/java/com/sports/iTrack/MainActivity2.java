//package com.sports.iTrack;
//
//import android.app.Activity;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.view.View;
//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationListener;
//import com.amap.api.location.LocationManagerProxy;
//import com.amap.api.location.LocationProviderProxy;
//import com.amap.api.maps2d.AMap;
//import com.amap.api.maps2d.LocationSource;
//import com.amap.api.maps2d.MapView;
//import com.amap.api.maps2d.model.BitmapDescriptor;
//import com.amap.api.maps2d.model.BitmapDescriptorFactory;
//import com.amap.api.maps2d.model.MyLocationStyle;
//
//public class MainActivity2 extends Activity implements View.OnClickListener, LocationSource,
//        AMapLocationListener {
//    private MapView mapView;
//    private AMap aMap;
//    private OnLocationChangedListener mListener;
//    private LocationManagerProxy mAMapLocationManager;
//    /**
//     * Called when the activity is first created.
//     */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
////        mapView = (MapView) findViewById(R.id.map);
//        mapView.onCreate(savedInstanceState);
//        init();
//    }
//
//    private void init(){
//
//        if (aMap == null) {
//            aMap = mapView.getMap();
//            setUpMap();
//        }
//    }
//
//    private void setUpMap(){
//        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(
//                BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
//        myLocationStyle.strokeColor(Color.GREEN);
//        myLocationStyle.strokeWidth(5);
//        aMap.setMyLocationStyle(myLocationStyle);
//        aMap.setLocationSource(this);
//        aMap.getUiSettings().setMyLocationButtonEnabled(true);
//        aMap.setMyLocationEnabled(true);
//    }
//
//    @Override public void onClick(View v) {
////
////        if (v.getId() == R.id.button) {
////            findViewById(R.id.textView).setVisibility(View.VISIBLE);
////        }
//    }
//
//    @Override protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mapView.onSaveInstanceState(outState);
//    }
//
//    @Override protected void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//
//    @Override public void activate(
//            OnLocationChangedListener onLocationChangedListener) {
//
//        mListener = onLocationChangedListener;
//        if (mAMapLocationManager == null) {
//            mAMapLocationManager = LocationManagerProxy.getInstance(this);
//			/*
//			 * mAMapLocManager.setGpsEnable(false);
//			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
//			 * API定位采用GPS和网络混合定位方式
//			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
//			 */
//            mAMapLocationManager.requestLocationUpdates(
//                    LocationProviderProxy.AMapNetwork, 2000, 10, this);
//        }
//    }
//
//    @Override public void deactivate() {
//
//        mListener = null;
//        if (mAMapLocationManager == null) {
//            mAMapLocationManager.removeUpdates(this);
//            mAMapLocationManager.destroy();
//        }
//        mAMapLocationManager = null;
//    }
//
//    @Override public void onLocationChanged(AMapLocation aMapLocation) {
//        if (mListener != null && aMapLocation != null) {
//            mListener.onLocationChanged(aMapLocation);
//        }
//
//    }
//
//    /**
//     * 此方法已经废弃
//     */
//    @Override public void onLocationChanged(Location location) {
//
//    }
//
//    @Override public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override public void onProviderDisabled(String provider) {
//
//    }
//}
