package com.sports.iTrack.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.sports.iTrack.ui.SliderRelativeLayout;
import com.sports.iTrack.utils.NetworkUtil;
import com.sports.iTrack.utils.TimeUtil;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
    private int previousPosition = 0;
    private int index = 0;
    private int mKal = 0;


    private double mCurrentAvgSpeed;
    private double mCurrentSpeed;
    private double mCurrentDistance = 0.0D;

    private boolean isFirstLoc = true;
    private boolean startTrack = false;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private LocationClient mLocClient;
    private Button mBtStart;
    private Button mBtEnd;
    private Button mBtGoon;
    private LinearLayout mllEndGoon;
    private TextView tv_speed;
    private TextView tv_duration_hour;
    private TextView tv_duration_min;
    private TextView tv_duration_second;
    private TextView tv_distance;
    private TextView tv_cal;

    private SliderRelativeLayout sliderLayout = null;
    private ImageView imgView_getup_arrow;
    private AnimationDrawable animArrowDrawable = null;


    private TrackItem trackItem = new TrackItem();
    private MyLocationListenner myListener = new MyLocationListenner();
    private List<LatLng> backupPts = new ArrayList<LatLng>();
    private SparseArray<BDLocation> locationSparseArray = new SparseArray<BDLocation>();

    private MyThread mCounterThread;
    private long mExitTime;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        NetworkUtil.openGPS(this);//强制打开GPS

        mBtEnd = (Button) findViewById(R.id.bt_end);
        mBtGoon = (Button) findViewById(R.id.bt_goon);
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
        imgView_getup_arrow = (ImageView)findViewById(R.id.getup_arrow);
        animArrowDrawable = (AnimationDrawable) imgView_getup_arrow.getBackground() ;
        sliderLayout.setMainHandler(handler);

        mBtStart.setVisibility(View.VISIBLE);
        mllEndGoon.setVisibility(View.GONE);
        sliderLayout.setVisibility(View.GONE);

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

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

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

            if (startTrack) {

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                backupPts.add(latLng);

                /**
                 * 生成 RecordPoint 对象，并保存到SparseArray
                 */
                RecordPoint recodPoint = new RecordPoint();

                double distance = 0.0D;

                if (locationSparseArray.size() <= 1) {
                    recodPoint.setDistance(0);
                } else {
                    distance = DistanceUtil.getDistance(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            new LatLng(locationSparseArray.get(index - 1).getLatitude(),
                                    locationSparseArray.get(index - 1).getLongitude()));
                    recodPoint.setDistance(distance);
                }

                recodPoint.setSpeed(location.getSpeed());
                recodPoint.setAltitude(location.getAltitude());
                recodPoint.setLatitude(location.getLatitude());
                recodPoint.setLongitude(location.getLongitude());
                recodPoint.setTimestamp(System.currentTimeMillis());
                //recodPoint.save();
                trackItem.getRecordPointList().add(recodPoint);

                /**
                 * 保存location，用于计算distance
                 */
                locationSparseArray.put(index, location);
                index++;

                //刷新界面，显示距离(KM) 以及当前速度
                mCurrentDistance += distance;
                tv_distance.setText(Double.toString(TimeUtil.formatData(mCurrentDistance / 1000)));

                mCurrentSpeed = TimeUtil.formatData(location.getSpeed());
                tv_speed.setText(Double.toString(mCurrentSpeed));

                //实时显示路径,每四步 画一次
                if (backupPts.size() >= previousPosition + 4) {
                    List<LatLng> temp = new ArrayList<LatLng>();
                    for (int i = previousPosition; i < backupPts.size(); i++) {
                        temp.add(backupPts.get(i));
                    }

                    OverlayOptions polylineOption = new PolylineOptions().color(0xAA0000FF).width(
                            10).points(temp);
                    mBaiduMap.addOverlay(polylineOption);

                    previousPosition = backupPts.size() - 1;
                    temp.clear();
                }
            }

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override public void
    onClick(View v) {
        if (v.getId() == R.id.button2) {
            mBaiduMap.clear();
            if (backupPts == null)
                return;
            OverlayOptions polylineOption = new PolylineOptions().color(0xAA0000FF).width(
                    10).points(backupPts);
            mBaiduMap.addOverlay(polylineOption);
        } else if (v.getId() == R.id.bt_start) {
            /**
             * TODO 判断GPS信号强弱，太弱则作提示
             */
            if (!NetworkUtil.isOpenGPS(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("GPS 尚未打开，是否打开?")
                        .setCancelable(false)
                        .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                NetworkUtil.openGPS(getApplicationContext());
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                return;
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                mBtStart.setVisibility(View.GONE);
                mllEndGoon.setVisibility(View.GONE);
                sliderLayout.setVisibility(View.VISIBLE);

                setClickable(false);

                if (mCounterThread == null) {
                    mCounterThread = new MyThread();
                    mCounterThread.start();
                }

//                startTrack = true;

                /**
                 * 线程是否 挂起的，如果挂起，则恢复
                 */

                if (mCounterThread.isSuspend()) {
                    mCounterThread.doResume();
                }
                /**
                 * 只调一次，后续自动保存数据
                 */
                saveRecodPoint();
            }

        } else if (v.getId() == R.id.bt_end) {
            mBtStart.setVisibility(View.VISIBLE);
            mllEndGoon.setVisibility(View.GONE);
            sliderLayout.setVisibility(View.GONE);


            new SaveTrackItemTask().execute();

            /**
             * 如果线程没有挂起，则暂停线程
             */
            if (!mCounterThread.isSuspend()) {
                mCounterThread.doSuspend();
            }

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

            startTrack = true;
        }
    }

    private void setClickable(boolean clickable) {
        /**
         * 屏蔽所有事件
         */
        mMapView.setClickable(clickable);
        getActionBar().setHomeButtonEnabled(clickable);
        mBtStart.setClickable(clickable);
        mllEndGoon.setClickable(clickable);
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


    @Override protected void onResume() {
        super.onResume();
        mMapView.onResume();
        handler.postDelayed(AnimationDrawableTask, 300);
    }

    @Override protected void onPause() {
        super.onPause();
        mMapView.onPause();
        animArrowDrawable.stop();
    }

    @Override protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

        super.onDestroy();
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


    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MSG_SECOND:
                    if (startTrack) {
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
                    if (mMin == 15 || mMin == 45 || mMin == 60) {
                        Message message0 = new Message();
                        message0.what = MSG_CAL;
                        handler.sendMessage(message0);
                    }

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
                    int t = mHour * 60 * 60 + mMin * 60 + mSecond;
                    double avgSpeed = (mCurrentDistance / t) * (3600 / 1000);
                    mCurrentAvgSpeed = TimeUtil.formatData(avgSpeed);
                    mKal = TimeUtil.getKal(mCurrentAvgSpeed, (mHour * 2 + mMin), 65);
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
                    previousPosition = 0;
                    index = 0;
                    mCurrentAvgSpeed = 0;
                    mCurrentSpeed = 0;
                    mCurrentDistance = 0.0D;
                    isFirstLoc = true;
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Runnable AnimationDrawableTask = new Runnable(){

        public void run(){
            animArrowDrawable.start();
            handler.postDelayed(AnimationDrawableTask, 300);
        }
    };

    public class MyThread extends Thread{

        @Override public void run() {
            while(true){
                try {
                    if (startTrack) {
                        Thread.sleep(1000);     // sleep 1000ms
                        Message message = new Message();
                        message.what = MSG_SECOND;
                        handler.sendMessage(message);
                    } else {
                        wait();
                    }
                }catch (Exception e) {
                }
            }
        }

        public void doSuspend() {
            startTrack = false;
        }

        public void doResume() {
            startTrack = true;
        }

        public boolean isSuspend() {
            return !startTrack;
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

            List<RecordPoint> recordPoints = trackItem.getRecordPointList();
            if (recordPoints == null || recordPoints.size() == 0)
                return "no data to save.";
            long startTime = recordPoints.get(0).getTimestamp();
            long endTime = recordPoints.get(recordPoints.size() - 1).getTimestamp();

            double distance = 0.0D;

            ArrayList<Float> speeds = new ArrayList<Float>();
            ArrayList<Double> altitudes = new ArrayList<Double>();
            double speed = 0.0D;
            for (int i = 0; i < recordPoints.size(); i++) {
                //                speed += recordPoints.get(i).getSpeed();
                speeds.add(recordPoints.get(i).getSpeed());
                altitudes.add(recordPoints.get(i).getAltitude());

                distance += recordPoints.get(i).getDistance();
            }

            /*if (distance == 0) {
                return "运动距离为0，不保存数据";
            }*/

            distance = TimeUtil.formatData(distance);

            /**
             * time 包含了中途停止的时间
             * avgSpeed 单位：km/h
             *
             * 为了避免time 过短 avgSpeed 趋于无穷，
             * 先计算m/s ，再转化为 km/h
             */
            //全程耗时,单位:秒
            double time = (endTime - startTime) / 1000;
            double avgSpeed = (distance / time) * (3600 / 1000);
            avgSpeed = TimeUtil.formatData(avgSpeed);

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
            trackItem.setKal(mKal);
            trackItem.save();
            return "保存成功";
        }

        @Override protected void onPostExecute(String s) {
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            /**
             * 运动结束，保存数据之后，清除数据；
             */
            if (!startTrack) {
                backupPts.clear();
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
    public void saveRecodPoint() {
        final Runnable runnable = new Runnable() {
            public void run() {
                if (locationSparseArray == null || trackItem == null) {
                    return;
                }

                /**
                 * 用于保存所有数据
                 */
                DataSupport.saveAll(trackItem.getRecordPointList());

                /**
                 * 保存完了之后，清理掉数据, 为了防止容器过多对象，
                 * TODO： locationSparseArray 最后一个location 与下一次计算的location 直接的distance 为 0
                 */
                //                List<BDLocation> temp = new ArrayList<BDLocation>();
                //                temp.add(locationSparseArray.get(locationSparseArray.size() - 1));
                locationSparseArray.clear();
                //                locationSparseArray.put(0, temp.get(0));

                //                trackItem.getRecordPointList().clear();
            }
        };
        final ScheduledFuture saveRecodPointHandle =
                scheduler.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

}
