package com.sports.iTrack.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.sports.iTrack.R;
import com.sports.iTrack.base.BaseActivity;
import com.sports.iTrack.model.RecordPoint;
import com.sports.iTrack.model.TrackItem;
import com.sports.iTrack.service.LockService;
import com.sports.iTrack.ui.SliderRelativeLayout;
import com.sports.iTrack.utils.NetworkUtils;
import com.sports.iTrack.utils.TimeUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity
        implements View.OnClickListener {

    private static final double ERROR_RATE = 0.009;//about 1km
    private static final int MSG_SECOND = 1;
    private static final int MSG_MIN = 2;
    private static final int MSG_HOUR = 3;
    private static final int MSG_CAL = 10;
    private static final int MSG_DISTANCE = 11;
    private static final int MSG_SPEED = 12;
    private static final int MSG_GPS = 13;
    public static final int MSG_CLEAR_LOCK_SUCESS = 14;
    private static final int MSG_RESET = 15;

    private int mSecond = 0;
    private int mMin = 0;
    private int mHour = 0;
    private int index = 0;
    private int mKal = 0;
    //如果倒计时20s 速度一直为0，暂停运动
    private int mTrackPauseLimited = 20;

    private long mExitTime;

    /**
     * 由气压传感器计算的海拔;
     */
    private double mHeight;
    private double mCurrentSpeed;
    private double mCurrentDistance = 0.0D;

    private boolean isFirstLoc = true;
    private boolean startTrack = false;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private LocationClient mLocClient;
    private Button mBtStart;
    private LinearLayout mllEndGoon;
    private TextView tv_speed, tv_duration_hour, tv_duration_min, tv_duration_second, tv_distance, tv_cal;

    private SliderRelativeLayout sliderLayout = null;
    private AnimationDrawable animArrowDrawable = null;


    private TrackItem trackItem = null;
    private MyLocationListenner myListener = new MyLocationListenner();

    /**
     * 存放 画线之后的 最后一个点，作为下一次画线的第一个点
     */
    private LatLng lastLatLng = null;
    private SparseArray<BDLocation> locationSparseArray = new SparseArray<BDLocation>();
    private List<LatLng> tempLatLngList = new ArrayList<LatLng>();
    private MyThread mCounterThread = new MyThread();

    private SensorManager mSensorManager;
    private Sensor mPressure;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();

        initMapAndLocClient();
    }

    @Override protected void onResume() {
        super.onResume();
        mMapView.onResume();
        handler.postDelayed(AnimationDrawableTask, 300);

        if (mPressure != null) {
            mSensorManager.registerListener(mPressureListener, mPressure,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override protected void onPause() {
        super.onPause();
        mMapView.onPause();
        animArrowDrawable.stop();

        if (mPressure != null) {
            mSensorManager.unregisterListener(mPressureListener);
        }
    }

    @Override protected void onDestroy() {
        Intent intent = new Intent(this, LockService.class);
        intent.putExtra("trackStartFlag", startTrack);
        this.stopService(intent);

        mLocClient.unRegisterLocationListener(myListener);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

        super.onDestroy();
    }

    private double calcAltitude(BDLocation bdLocation, Sensor sensor) {
        double altitude = 0;
        if (bdLocation.hasAltitude()) {
            altitude = bdLocation.getAltitude();
        } else if (sensor != null){
            // TODO 暂时不考虑海拔低于0的情况
            altitude = Math.abs(mHeight);
        }

        return altitude;

    }

    private SensorEventListener mPressureListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (startTrack) {
                float sPV = event.values[0];
                mHeight = 44330000 * (1 - (Math.pow((TimeUtils.formatData(sPV) / 1013.25),
                        (float) 1.0 / 5255.0)));
                mHeight = TimeUtils.formatData(mHeight);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /**
     * 定位SDK监听函数
     */
    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            showLocOnFirst(location);

            mCurrentSpeed = TimeUtils.formatData(location.getSpeed());
            if (mCurrentSpeed != 0) {
                mCounterThread.doResume();
            }

            if (startTrack && !mCounterThread.isSuspend()) {

                /**
                 * baidu 定位失败返回的默认经度
                 */
                if ((location.getLatitude() == 5e-324) || (location.getLongitude() == 5e-324)
                        || (location.getLatitude() == 4.9e-324) || (location.getLongitude() == 4.9e-324)) {
                    return;
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (lastLatLng != null) {
                    tempLatLngList.add(lastLatLng);
                }
                tempLatLngList.add(latLng);

                /**
                 * 生成 RecordPoint 对象，并保存到SparseArray
                 */
                RecordPoint recodPoint = new RecordPoint();

                double distance = 0.0D;

                if (locationSparseArray.size() <= 1) {
                    recodPoint.setDistance(0);
                } else {
                    distance = DistanceUtil.getDistance(latLng,
                            new LatLng(locationSparseArray.get(index - 1).getLatitude(),
                                    locationSparseArray.get(index - 1).getLongitude()));
                    recodPoint.setDistance(distance);
                }

                recodPoint.setSpeed(location.getSpeed());
                recodPoint.setAltitude(calcAltitude(location, mPressure));
                recodPoint.setLatitude(location.getLatitude());
                recodPoint.setLongitude(location.getLongitude());
                recodPoint.setTimestamp(System.currentTimeMillis());
                //recodPoint.save();
                trackItem.getRecordPointList().add(recodPoint);

                locationSparseArray.put(index, location);
                index++;

                //刷新界面，显示距离(KM) 以及当前速度
                mCurrentDistance += distance;
                tv_distance.setText(Double.toString(TimeUtils.formatData(mCurrentDistance / 1000)));

                tv_speed.setText(Double.toString(mCurrentSpeed));


                if (tempLatLngList.size() >= 4) {
                    OverlayOptions polylineOption = new PolylineOptions().color(0xAA0000FF).width(
                            10).points(tempLatLngList);
                    mBaiduMap.addOverlay(polylineOption);
                    lastLatLng = tempLatLngList.get(tempLatLngList.size() - 1);
                    tempLatLngList.clear();
                }
            }

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override public void
    onClick(View v) {
        if (v.getId() == R.id.button2) {
            //for test
        } else if (v.getId() == R.id.bt_start) {

            if (!NetworkUtils.isOpenGPS(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("GPS 尚未打开，是否打开?")
                        .setCancelable(false)
                        .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                NetworkUtils.openGPS(getApplicationContext());
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                mBtStart.setVisibility(View.GONE);
                mllEndGoon.setVisibility(View.GONE);
                sliderLayout.setVisibility(View.VISIBLE);

                setClickable(false);

                trackItem = new TrackItem();
                startTrack = true;

                if (mCounterThread.isSuspend()) {
                    mCounterThread.doResume();
                }
                if (!mCounterThread.isAlive()) {
                    mCounterThread.start();
                }

//                Intent intent = new Intent(this, LockService.class);
//                intent.putExtra("trackStartFlag", startTrack);
//                this.startService(intent);

                /**
                 * 只调一次，后续自动保存数据
                 */
                saveRecodPoint();
            }

        } else if (v.getId() == R.id.bt_end) {
            mBtStart.setVisibility(View.VISIBLE);
            mllEndGoon.setVisibility(View.GONE);
            sliderLayout.setVisibility(View.GONE);

            startTrack = false;

            if (!mCounterThread.isSuspend()) {
                mCounterThread.doSuspend();
            }

            Intent intent = new Intent(this, LockService.class);
            intent.putExtra("trackStartFlag", startTrack);
            this.stopService(intent);

            new SaveTrackItemTask().execute();

            /**
             * 清除view 显示数据
             */
            Message message = new Message();
            message.what = MSG_RESET;
            handler.sendMessage(message);

        } else if (v.getId() == R.id.bt_goon) {
            mBtStart.setVisibility(View.GONE);
            mllEndGoon.setVisibility(View.GONE);
            sliderLayout.setVisibility(View.VISIBLE);

            if (mCounterThread.isSuspend()) {
                mCounterThread.doResume();
            }
        }
    }

    public boolean onKeyDown(int keyCode ,KeyEvent event){

        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if (!startTrack) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }

                return true;
            } else {
                return false;
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MSG_SECOND:
                    if (startTrack && !mCounterThread.isSuspend()) {
                        mSecond++;
                        if (mSecond < 10) {
                            tv_duration_second.setText("0" + mSecond);
                        } else if (mSecond > 9 && mSecond < 60) {
                            tv_duration_second.setText("" + mSecond);
                        } else if (mSecond > 59) {
                            mSecond = 0;
                            tv_duration_second.setText("0" + mSecond);
                            mMin++;
                            Message message = new Message();
                            message.what = MSG_MIN;
                            handler.sendMessage(message);
                        }
                    }

                    break;
                case MSG_MIN:
                    if (mMin == 5 || mMin == 10 || mMin == 25 || mMin == 45 || mMin == 55) {
                        mKal = calculateKal();

                        Message message0 = new Message();
                        message0.what = MSG_CAL;
                        handler.sendMessage(message0);
                    }

                    Log.e("lushuifei", "locationSparseArray size:"+locationSparseArray.size());
                    if (mMin < 10) {
                        tv_duration_min.setText("0" + mMin);
                    } else if (mMin > 9 && mMin < 60) {
                        tv_duration_min.setText("" + mMin);
                    } else if (mMin > 59) {
                        mMin = 0;
                        tv_duration_min.setText("0" + mMin);
                        mHour++;
                        Message message = new Message();
                        message.what = MSG_HOUR;
                        handler.sendMessage(message);
                    }

                    break;
                case MSG_HOUR:
                    if (mHour < 9) {
                        tv_duration_hour.setText("0" + mHour);
                    } else {
                        tv_duration_hour.setText("" + mHour);
                    }
                    break;
                case MSG_CAL:
                    tv_cal.setText(mKal + "");

                    break;
                case MSG_DISTANCE:

                    break;
                case MSG_SPEED:

                    break;
                case MSG_GPS:

                    break;
                case MSG_CLEAR_LOCK_SUCESS:
                    mBtStart.setVisibility(View.GONE);
                    mllEndGoon.setVisibility(View.VISIBLE);
                    sliderLayout.setVisibility(View.GONE);
                    setClickable(true);
                    break;
                case MSG_RESET:
                    reset();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private int calculateKal(){
        int t = mHour * 60 * 60 + mMin * 60 + mSecond;
        double avgSpeed = (mCurrentDistance / t) * (3600 / 1000);
        double mCurrentAvgSpeed = TimeUtils.formatData(avgSpeed);
        return TimeUtils.getKal(mCurrentAvgSpeed, (mHour * 2 + mMin), 65);
    }

    private Runnable AnimationDrawableTask = new Runnable(){

        public void run(){
            animArrowDrawable.start();
            handler.postDelayed(AnimationDrawableTask, 300);
        }
    };

    private class MyThread extends Thread{

        private boolean mPauseTrack = false;

        @Override public void run() {
            while(true){
                try {
                    if (startTrack && !mPauseTrack) {
                        Thread.sleep(1000);

                        if (mCurrentSpeed == 0 && --mTrackPauseLimited < 0) {
                            mPauseTrack = true;
                            mTrackPauseLimited = 15;
                        } else {
                            mPauseTrack = false;
                            Message message = new Message();
                            message.what = MSG_SECOND;
                            handler.sendMessage(message);
                        }

                    } else {
                        //wait();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void doSuspend() {
            mPauseTrack = true;
        }

        public void doResume() {
            mPauseTrack = false;
        }

        public boolean isSuspend() {
            return mPauseTrack;
        }


    }

    /**
     * 异步保存trackItem 数据
     * 保存时机：
     * 1. 触发终止记录；
     * 2. 异常情况，退到后台，异常退出？
     */
    private class SaveTrackItemTask extends AsyncTask<Void, Void, String> {

        @Override protected String doInBackground(Void... params) {
            return saveTrackItem();
        }

        @Override protected void onPostExecute(String s) {
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            if (!startTrack) {
//                backupPts.clear();
                locationSparseArray.clear();
                mBaiduMap.clear();
            }
            super.onPostExecute(s);
        }
    }

    /**
     * 创建线程池，用于保存RecordPoint 数据，
     * 保存数据的时机是：
     * 1. 定时且数据量达到规定数目
     * 2. 退到后台
     * 3. 异常退出
     */
    private void saveRecodPoint() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final Runnable runnable = new Runnable() {
            public void run() {
                if (locationSparseArray == null || trackItem == null) {
                    return;
                }

                DataSupport.saveAll(trackItem.getRecordPointList());

                locationSparseArray.clear();
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }


    private void initView(){
        Button mBtEnd = (Button) findViewById(R.id.bt_end);
        Button mBtGoon = (Button) findViewById(R.id.bt_goon);
        mBtStart = (Button) findViewById(R.id.bt_start);
        mBtEnd.setOnClickListener(this);
        mBtGoon.setOnClickListener(this);
        mBtStart.setOnClickListener(this);


        mMapView = (MapView) findViewById(R.id.bmapView);
        tv_cal = (TextView) findViewById(R.id.tv_kcal_content);
        tv_distance = (TextView) findViewById(R.id.tv_distance_content);
        tv_speed = (TextView) findViewById(R.id.tv_speed_content);
        tv_duration_hour = (TextView) findViewById(R.id.tv_duration_hour);
        tv_duration_min = (TextView) findViewById(R.id.tv_duration_min);
        tv_duration_second = (TextView) findViewById(R.id.tv_duration_second);

        mllEndGoon = (LinearLayout) findViewById(R.id.ll_end_goon);
        sliderLayout = (SliderRelativeLayout)findViewById(R.id.slider_layout);
        ImageView imgView_getup_arrow = (ImageView) findViewById(R.id.getup_arrow);
        animArrowDrawable = (AnimationDrawable) imgView_getup_arrow.getBackground() ;
        sliderLayout.setMainHandler(handler);

        mBtStart.setVisibility(View.VISIBLE);
        mllEndGoon.setVisibility(View.GONE);
        sliderLayout.setVisibility(View.GONE);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if(mPressure == null) {
                Toast.makeText(this, "您的手机不支持气压传感器.", Toast.LENGTH_LONG);
            }
        }
    }

    private void initMapAndLocClient(){
        mBaiduMap = mMapView.getMap();

        //        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(
        //                R.drawable.location_marker);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapStatus(
                MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(16).build()));

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
        option.setAddrType("all");
        option.setPriority(LocationClientOption.GpsFirst);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void setClickable(boolean clickable) {
        /**
         * 屏蔽所有事件
         */
        mMapView.setClickable(clickable);
        //noinspection ConstantConditions
        getActionBar().setHomeButtonEnabled(clickable);
        mBtStart.setClickable(clickable);
        mllEndGoon.setClickable(clickable);
    }

    private void showLocOnFirst(BDLocation location){
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMapView == null)
            return;

        MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                .direction(location.getDerect()).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();

        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        }
    }

    private void reset(){
        tv_duration_second.setText("00");
        tv_duration_min.setText("00");
        tv_duration_hour.setText("00");
        tv_cal.setText("0");
        tv_speed.setText("0.00");
        tv_distance.setText("0.00");

        startTrack = false;
        mSecond = 0;
        mMin = 0;
        mHour = 0;
        index = 0;
        mCurrentSpeed = 0;
        mCurrentDistance = 0.0D;
        mKal = 0;
        isFirstLoc = true;
    }

    private String saveTrackItem() {
        String saveResult = "";
        List<RecordPoint> recordPoints = trackItem.getRecordPointList();
        if (recordPoints == null || recordPoints.size() == 0) {
            saveResult = "没有数据，保存失败";
            return saveResult;
        }

        long startTime = recordPoints.get(0).getTimestamp();
        long endTime = recordPoints.get(recordPoints.size() - 1).getTimestamp();

        double distance = 0.0D;

        ArrayList<Float> speeds = new ArrayList<Float>();
        ArrayList<Double> altitudes = new ArrayList<Double>();
        for (int i = 0; i < recordPoints.size(); i++) {
            speeds.add(recordPoints.get(i).getSpeed());
            altitudes.add(recordPoints.get(i).getAltitude());

            distance += recordPoints.get(i).getDistance();
        }

        if (distance == 0) {
            saveResult = "运动距离太短，不保存数据";
            return saveResult;
        }

        distance = TimeUtils.formatData(distance);

        /**
         * time 包含了中途停止的时间
         * avgSpeed 单位：km/h
         *
         * 为了避免time 过短 avgSpeed 趋于无穷，
         * 先计算m/s ，再转化为 km/h
         */
        //全程耗时,单位:秒
        double time = mHour * 3600 + mMin * 60 + mSecond;
        double avgSpeed = (distance / time) * (3600 / 1000);
        avgSpeed = TimeUtils.formatData(avgSpeed);

        Collections.sort(speeds);
        double maxSpeed = speeds.get(recordPoints.size() - 1);
        double minSpeed = speeds.get(0);

        Collections.sort(altitudes);
        double maxAltitude = altitudes.get(recordPoints.size() - 1);
        double minAltitude = altitudes.get(0);

        int sportTpye = 1;
        int recordPointsCount = recordPoints.size();

        String discription = "我de骑行记录";
        long timestamp = System.currentTimeMillis();

        trackItem.setStartTime(startTime);
        trackItem.setDuration(time);
        trackItem.setEndTime(endTime);
        trackItem.setDistance(distance);
        trackItem.setAvgSpeed(avgSpeed);
        trackItem.setMaxSpeed(maxSpeed);
        trackItem.setMinSpeed(minSpeed);
        trackItem.setMaxAltitude(maxAltitude);
        trackItem.setMinAltitude(minAltitude);
        trackItem.setSportTpye(sportTpye);
        trackItem.setRecordPointsCount(recordPointsCount);
        trackItem.setDiscription(discription);
        trackItem.setTimestamp(timestamp);
        trackItem.setKal(mKal == calculateKal() ? mKal : calculateKal());
        trackItem.save();
        trackItem = null;
        saveResult = "保存成功";
        return saveResult;
    }

    private boolean isReasonableLocation(BDLocation lastLocation, BDLocation newLocation) {
        double lngRate = Math.abs(lastLocation.getLongitude() - newLocation.getLongitude());
        double latRate = Math.abs(lastLocation.getLatitude() - newLocation.getLatitude());
        if (lngRate > ERROR_RATE || latRate > ERROR_RATE) {
            return false;
        } else {
            return true;
        }
    }
}
