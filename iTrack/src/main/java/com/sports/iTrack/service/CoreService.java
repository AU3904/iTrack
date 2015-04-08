package com.sports.iTrack.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.sports.iTrack.R;
import com.sports.iTrack.activity.MainActivity;
import com.sports.iTrack.model.OneTrack;
import com.sports.iTrack.model.RecordPoint;
import com.sports.iTrack.model.Time;
import com.sports.iTrack.model.TrackItem;
import com.sports.iTrack.model.TrackLocation;
import com.sports.iTrack.utils.TimeUtils;
import com.sports.iTrack.utils.constant;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用来监听气压 和 location 变化；并且做数据持久化操作
 * 同时计算响应的值，通知MainActivity ui 显示；
 */
public class CoreService extends IntentService {

    private LocationBinder locationBinder = new LocationBinder();
    private MyLocationListenner myListener = new MyLocationListenner();
    private LocationClient mLocClient;
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private TrackItem trackItem = null;

    /**
     * 由气压传感器计算的海拔;
     */
    private double mHeight;

    private double mCurrentDistance = 0.0D;

    private int index = 0;
    private int mKal = 0;

    private OneTrack mOneTrack = OneTrack.getInstance();

    private Time mTime = Time.getInstance();
    /**
     * 存放 画线之后的 最后一个点，作为下一次画线的第一个点
     */
    private LatLng lastLatLng = null;
    private SparseArray<BDLocation> locationSparseArray = new SparseArray<BDLocation>();
    private ArrayList<LatLng> latLngArrayList = new ArrayList<LatLng>();

    public CoreService() {
        super("CoreService");
    }

    @Override
    public void setIntentRedelivery(boolean enabled) {
        super.setIntentRedelivery(enabled);
    }


    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("lushuifei", "on onLowMemory");

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("lushuifei", "on onBind");
        Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.app_name),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.notification_message), pendingIntent);
        startForeground(Notification.FLAG_ONGOING_EVENT, notification);

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

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if (mPressure == null) {
                Toast.makeText(this, "您的手机不支持气压传感器.", Toast.LENGTH_LONG).show();
            }
        }
        if (mPressure != null) {
            assert mSensorManager != null;
            mSensorManager.registerListener(mPressureListener, mPressure,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        return this.locationBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("lushuifei", "on onUnbind");
        mLocClient.unRegisterLocationListener(myListener);
        // 退出时销毁定位
        mLocClient.stop();
        if (mPressure != null) {
            mSensorManager.unregisterListener(mPressureListener);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e("lushuifei", "on onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("lushuifei", "on onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }


    public class LocationBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {

            String cmd = data.readString();
            if (constant.CMD_NEW_TRACK_ITEM.equalsIgnoreCase(cmd)) {
                trackItem = new TrackItem();
            } else if (constant.CMD_SAVE_RECORD_POINT.equalsIgnoreCase(cmd)) {
                saveRecodPoint();
            } else if (constant.CMD_EXE_SAVE_TRACK_ITEM.equalsIgnoreCase(cmd)) {
                new SaveTrackItemTask().execute();
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    /**
     * 定位SDK监听函数
     */
    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            TrackLocation trackLocation = TrackLocation.getInstance();

            if (mOneTrack.isFirstLocation()) {
                trackLocation.setBdLocation(location);

                Intent intent = new Intent(constant.ACTION_UPDATE_UI);
                intent.putExtra(constant.KEY_FLAG, constant.FLAG_FIRST_LOCATION);
                sendBroadcast(intent);
            }

            double mCurrentSpeed = TimeUtils.formatData(location.getSpeed());
            if (mCurrentSpeed != 0) {
                mOneTrack.resume();
            }


            if (mOneTrack.isStart() && !mOneTrack.isPause()) {
                if ((location.getLatitude() == 5e-324) || (location.getLongitude() == 5e-324)
                        || (location.getLatitude() == 4.9e-324) || (location.getLongitude() == 4.9e-324)) {
                    return;
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (lastLatLng != null) {
                    latLngArrayList.add(lastLatLng);
                }
                latLngArrayList.add(latLng);

                RecordPoint recordPoint = new RecordPoint();

                double distance = 0.0D;

                if (locationSparseArray.size() <= 1) {
                    recordPoint.setDistance(0);
                } else {
                    distance = DistanceUtil.getDistance(latLng,
                            new LatLng(locationSparseArray.get(index - 1).getLatitude(),
                                    locationSparseArray.get(index - 1).getLongitude()));
                    recordPoint.setDistance(distance);
                }

                recordPoint.setSpeed(location.getSpeed());
                recordPoint.setAltitude(calcAltitude(location, mPressure));
                recordPoint.setLatitude(location.getLatitude());
                recordPoint.setLongitude(location.getLongitude());
                recordPoint.setTimestamp(System.currentTimeMillis());
                trackItem.getRecordPointList().add(recordPoint);

                locationSparseArray.put(index, location);

                if (latLngArrayList.size() >= 4) {

                    trackLocation.setBdLocationArrayList(latLngArrayList);
                    Intent intent = new Intent(constant.ACTION_UPDATE_UI);
                    intent.putExtra(constant.KEY_FLAG, constant.FLAG_POLY);
                    sendBroadcast(intent);

                    lastLatLng = latLngArrayList.get(latLngArrayList.size() - 1);
                    latLngArrayList.clear();

                    mKal = mKal == calculateKal() ? mKal : calculateKal();
                }

                index++;

                mCurrentDistance += distance;
                Intent intent = new Intent(constant.ACTION_UPDATE_UI);
                intent.putExtra(constant.KEY_FLAG, constant.FLAG_UI);
                intent.putExtra(constant.KEY_DISTANCE, TimeUtils.formatData(mCurrentDistance / 1000));
                intent.putExtra(constant.KEY_SPEED, mCurrentSpeed);
                intent.putExtra(constant.KEY_KAL, mKal);
                sendBroadcast(intent);

            }


        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    private SensorEventListener mPressureListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mOneTrack.isStart()) {
                float sPV = event.values[0];
                mHeight = TimeUtils.formatData((44330000 * (1 - (Math.pow((TimeUtils.formatData(sPV) / 1013.25),
                        (float) 1.0 / 5255.0)))));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    private double calcAltitude(BDLocation bdLocation, Sensor sensor) {
        double altitude = 0;
        if (bdLocation.hasAltitude()) {
            altitude = bdLocation.getAltitude();
        } else if (sensor != null) {
            // TODO 暂时不考虑海拔低于0的情况
            altitude = Math.abs(mHeight);
        }

        return altitude;

    }

    private int calculateKal() {
        double t = mTime.getTotalSeconds();
        double avgSpeed = (mCurrentDistance / t) * (3600 / 1000);
        double mCurrentAvgSpeed = TimeUtils.formatData(avgSpeed);
        return TimeUtils.getKal(mCurrentAvgSpeed, (mTime.getHour() * 2 + mTime.getMin()), 65);
    }

    /**
     * 异步保存trackItem 数据
     * 保存时机：
     * 1. 触发终止记录；
     * 2. 异常情况，退到后台，异常退出？
     */
    private class SaveTrackItemTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return saveTrackItem();
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(CoreService.this, s, Toast.LENGTH_LONG).show();
            if (!mOneTrack.isStart()) {
                locationSparseArray.clear();
                mTime.reset();
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


    private String saveTrackItem() {
        String saveResult;
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
        double time = mTime.getTotalSeconds();
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

    private static final double ERROR_RATE = 0.009;//about 1km
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
