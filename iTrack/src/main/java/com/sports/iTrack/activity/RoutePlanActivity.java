package com.sports.iTrack.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.*;
import com.baidu.mapapi.search.route.*;
import com.sports.iTrack.base.BaseActivity;
import com.sports.iTrack.R;
import com.sports.iTrack.utils.TimeUtils;

/**
 * Created by aaron_lu on 2/3/15.
 */
public class RoutePlanActivity extends BaseActivity
        implements OnGetRoutePlanResultListener, BaiduMap.OnMapClickListener {

    private BaiduMap mBaiduMap = null;
    private MapView mMapView = null;
    private LocationClient mLocClient;
    private boolean useDefaultIcon = true;
    private boolean isFirstLoc = true;

    private Marker mMarkerA;
    private BitmapDescriptor bdGround = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gcoding);

    private LatLng mStartLatLng = null;
    private LatLng mEndLatLng = null;
    private InfoWindow mInfoWindow;
    private RoutePlanSearch mSearch = null;
    private String mCurrentCity = null;

    private TextView mTvStart;
    private TextView mTvEnd;
    private EditText mEtStart;
    private EditText mEtEnd;

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_plan_layout);

        mTvStart = (TextView) findViewById(R.id.textView_start);
        mTvEnd = (TextView) findViewById(R.id.textView_end);
        mEtStart = (EditText) findViewById(R.id.start);
        mEtEnd = (EditText) findViewById(R.id.end);
        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setOnMapClickListener(this);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                Button button = new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.popup);
                InfoWindow.OnInfoWindowClickListener listener = null;
                if (marker == mMarkerA) {
                    button.setText("将这里设置为终点");
                    button.setTextColor(Color.parseColor("#A52A2A"));
                    listener = new InfoWindow.OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                            LatLng ll = marker.getPosition();
                            mEndLatLng = ll;
                            //mEditEnd.setText(mEndLatLng.toString());
                            //mBaiduMap.hideInfoWindow();
                        }
                    };
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47,
                            listener);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
                return true;
            }
        });

        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);


        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapStatus(
                MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(16).build()));

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null || mMapView == null)
                    return;

                MyLocationData locData = new MyLocationData.Builder().accuracy(bdLocation.getRadius())
                        .direction(bdLocation.getDerect()).latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();

                mBaiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(bdLocation.getLatitude(),
                            bdLocation.getLongitude());

                    mEtStart.setText(bdLocation.getAddrStr());
                    mStartLatLng = ll;
                    mCurrentCity = bdLocation.getCity();

                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }
            }

            @Override
            public void onReceivePoi(BDLocation bdLocation) {

            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
        option.setAddrType("all");
        option.setPriority(LocationClientOption.GpsFirst);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = getDrawerLayout().isDrawerOpen(getLeftDrawer());
        menu.findItem(R.id.action_change).setVisible(!drawerOpen);
        menu.findItem(R.id.action_bus_plan).setVisible(!drawerOpen);
        menu.findItem(R.id.action_car_plan).setVisible(!drawerOpen);
        menu.findItem(R.id.action_foot_plan).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //起点一般是我的位置
        PlanNode stNode = PlanNode.withLocation(mStartLatLng);
        PlanNode enNode = PlanNode.withLocation(mEndLatLng);
        if (mEndLatLng == null) {
            //默认选址是在当前城市
            enNode = PlanNode.withCityNameAndPlaceName(mCurrentCity, mEtEnd.getText().toString());
        }

        mBaiduMap.clear();

        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_change:

                LatLng temp = mStartLatLng;
                mStartLatLng = mEndLatLng;
                mEndLatLng = temp;


                if ("S".equalsIgnoreCase(mTvStart.getText().toString())) {
                    mTvStart.setText("E");
                    mTvStart.setTextColor(getResources().getColor(R.color.holo_red_dark));
                } else if ("E".equalsIgnoreCase(mTvStart.getText().toString())) {
                    mTvStart.setText("S");
                    mTvStart.setTextColor(getResources().getColor(R.color.holo_green_dark));
                }

                if ("E".equalsIgnoreCase(mTvEnd.getText().toString())) {
                    mTvEnd.setText("S");
                    mTvEnd.setTextColor(getResources().getColor(R.color.holo_green_dark));
                } else if ("S".equalsIgnoreCase(mTvEnd.getText().toString())) {
                    mTvEnd.setText("E");
                    mTvEnd.setTextColor(getResources().getColor(R.color.holo_red_dark));
                }

                return true;
            case R.id.action_bus_plan:
                if (mCurrentCity == null) {
                    return true;
                }
                mSearch.transitSearch(
                        new TransitRoutePlanOption().from(stNode).city(mCurrentCity).to(enNode));
                return true;
            case R.id.action_car_plan:
                mSearch.drivingSearch(new DrivingRoutePlanOption().from(stNode).to(enNode));
                return true;
            case R.id.action_foot_plan:
                mSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mBaiduMap.clear();
        String url =
                "http://api.map.baidu.com/geocoder/v2/?ak=wMrKjqDGuhiBGF42w9I8EEZo&callback=renderReverse&location="
                        + latLng.latitude + "," + latLng.longitude + "&output=json&pois=0";

        OverlayOptions ooA = new MarkerOptions().position(latLng).icon(bdGround)
                .zIndex(9).draggable(true);
        mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
        mEndLatLng = latLng;

        GeoCoder geoCoder = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            // 反地理编码查询结果回调函数
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(RoutePlanActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
                    return;
                }
                mEtEnd.setText(result.getAddress());
            }

            // 地理编码查询结果回调函数
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                    Toast.makeText(RoutePlanActivity.this, "抱歉，未能找到结果 lushuifei",
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        if (walkingRouteResult == null
                || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        } else if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(walkingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();

            Toast.makeText(RoutePlanActivity.this,
                    "线路距离:" + TimeUtils.formatData(
                            walkingRouteResult.getRouteLines().get(0).getDistance() / 1000)
                            + "km,耗时:" +
                            TimeUtils.formatTime(
                                    walkingRouteResult.getRouteLines().get(0).getDuration()),
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

        if (transitRouteResult == null
                || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        } else if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(transitRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();

            Toast.makeText(RoutePlanActivity.this,
                    "线路距离:" + TimeUtils.formatData(
                            transitRouteResult.getRouteLines().get(0).getDistance() / 1000)
                            + "km,耗时:" +
                            TimeUtils.formatTime(
                                    transitRouteResult.getRouteLines().get(0).getDuration()),
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

        if (drivingRouteResult == null
                || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        } else if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(drivingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();

            Toast.makeText(RoutePlanActivity.this,
                    "线路距离:" + TimeUtils.formatData(
                            drivingRouteResult.getRouteLines().get(0).getDistance() / 1000)
                            + "km,耗时:" +
                            TimeUtils.formatTime(
                                    drivingRouteResult.getRouteLines().get(0).getDuration()),
                    Toast.LENGTH_LONG).show();
        }

    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }
}
