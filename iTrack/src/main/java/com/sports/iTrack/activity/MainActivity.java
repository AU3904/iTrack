package com.sports.iTrack.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.sports.iTrack.R;
import com.sports.iTrack.base.BaseActivity;
import com.sports.iTrack.model.Time;
import com.sports.iTrack.model.TrackLocation;
import com.sports.iTrack.service.CoreService;
import com.sports.iTrack.model.OneTrack;
import com.sports.iTrack.ui.SliderRelativeLayout;
import com.sports.iTrack.utils.NetworkUtils;
import com.sports.iTrack.utils.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity
        implements View.OnClickListener {

    private double mSpeed;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private Button mBtStart;
    private LinearLayout mllEndGoon;
    private TextView tv_speed, tv_duration_hour, tv_duration_min, tv_duration_second, tv_distance, tv_cal;

    private SliderRelativeLayout sliderLayout = null;
    private AnimationDrawable animArrowDrawable = null;
    private CoreService.LocationBinder mBinder;
    private OneTrack mOneTrack = OneTrack.getInstance();
    private Time mTime = Time.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();

        initMapAndLocClient();

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, CoreService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(constant.ACTION_UPDATE_UI);
        this.registerReceiver(mUpdateUIReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        handler.postDelayed(AnimationDrawableTask, 300);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        animArrowDrawable.stop();
    }

    @Override
    protected void onDestroy() {
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

        unbindService(serviceConnection);
        unregisterReceiver(mUpdateUIReceiver);
        mOneTrack.destory();
        super.onDestroy();
    }


    @Override
    public void
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

                transact(constant.CMD_NEW_TRACK_ITEM);

                mOneTrack.start();

                if (mOneTrack.isPause()) {
                    mOneTrack.resume();
                }

                if (mOneTrack.isStart() && !mOneTrack.isPause()) {
                    handler.postDelayed(mCounterRunnable, 1000);
                }

                transact(constant.CMD_SAVE_RECORD_POINT);
            }

        } else if (v.getId() == R.id.bt_end) {
            mBtStart.setVisibility(View.VISIBLE);
            mllEndGoon.setVisibility(View.GONE);
            sliderLayout.setVisibility(View.GONE);

            mOneTrack.end();
            mOneTrack.resume();

            transact(constant.CMD_EXE_SAVE_TRACK_ITEM);

            reset();

        } else if (v.getId() == R.id.bt_goon) {
            mBtStart.setVisibility(View.GONE);
            mllEndGoon.setVisibility(View.GONE);
            sliderLayout.setVisibility(View.VISIBLE);

            if (mOneTrack.isPause()) {
                mOneTrack.resume();
            }
        }
    }


    private void transact(String cmd) {
        Parcel data = Parcel.obtain();
        data.writeString(cmd);
        Parcel reply = Parcel.obtain();
        try {
            if (mBinder != null) {
                mBinder.transact(0, data, reply, BIND_AUTO_CREATE);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private long mExitTime;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (!mOneTrack.isStart()) {
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

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case constant.MSG_CLEAR_LOCK_SUCESS:
                    mBtStart.setVisibility(View.GONE);
                    mllEndGoon.setVisibility(View.VISIBLE);
                    sliderLayout.setVisibility(View.GONE);
                    setClickable(true);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void sendMsg(Handler handler, int flag) {
        handler.removeMessages(flag);
        Message message = new Message();
        message.what = flag;
        handler.sendMessage(message);
    }

    private static final int PAUSE_LIMIT = 20; //20s
    private int mTrackPauseLimited = PAUSE_LIMIT;
    private Runnable mCounterRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSpeed == 0 && --mTrackPauseLimited < 0) {
                mOneTrack.pause();
                mTrackPauseLimited = PAUSE_LIMIT;
            } else {
                mOneTrack.resume();

                mTime.secondPlus();
                int second = mTime.getSecond();
                if (second < 10) {
                    tv_duration_second.setText("0" + second);
                } else if (second > 9 && second < 60) {
                    tv_duration_second.setText("" + second);
                } else if (second > 59) {
                    second = 0;
                    tv_duration_second.setText("0" + second);
                    mTime.minPlus();
                    int min = mTime.getMin();
                    if (min < 10) {
                        tv_duration_min.setText("0" + min);
                    } else if (min > 9 && min < 60) {
                        tv_duration_min.setText("" + min);
                    } else if (min > 59) {
                        mTime.setMin(0);
                        tv_duration_min.setText("0" + min);
                        mTime.hourPlus();
                        int hour = mTime.getHour();
                        if (hour < 9) {
                            tv_duration_hour.setText("0" + hour);
                        } else {
                            tv_duration_hour.setText("" + hour);
                        }
                    }
                }

                handler.postDelayed(this, 1000);
            }

        }
    };


    private Runnable AnimationDrawableTask = new Runnable() {

        public void run() {
            animArrowDrawable.start();
            handler.postDelayed(AnimationDrawableTask, 300);
        }
    };

    private void initView() {
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
        sliderLayout = (SliderRelativeLayout) findViewById(R.id.slider_layout);
        ImageView imgView_getup_arrow = (ImageView) findViewById(R.id.getup_arrow);
        animArrowDrawable = (AnimationDrawable) imgView_getup_arrow.getBackground();
        sliderLayout.setMainHandler(handler);

        mBtStart.setVisibility(View.VISIBLE);
        mllEndGoon.setVisibility(View.GONE);
        sliderLayout.setVisibility(View.GONE);
    }

    private void initMapAndLocClient() {
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

    private void showLocOnFirst(BDLocation location) {
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMapView == null)
            return;

        MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                .direction(location.getDerect()).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();

        mBaiduMap.setMyLocationData(locData);
        if (mOneTrack.isFirstLocation()) {
            mOneTrack.setFirstLocation(false);
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        }
    }

    private void reset() {
        tv_duration_second.setText("00");
        tv_duration_min.setText("00");
        tv_duration_hour.setText("00");
        tv_cal.setText("0");
        tv_speed.setText("0.00");
        tv_distance.setText("0.00");

        mOneTrack.setFirstLocation(true);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainActivity.this.mBinder = (CoreService.LocationBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private TrackLocation trackLocation = TrackLocation.getInstance();
    private BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "UpdateUIReceiver receive", Toast.LENGTH_SHORT).show();
            switch (intent.getIntExtra(constant.KEY_FLAG, -1)) {
                case constant.FLAG_POLY:
                    List<LatLng> tempLatLngList = trackLocation.getBdLocationArrayList();
                    if (tempLatLngList != null && tempLatLngList.size() > 0) {
                        OverlayOptions polylineOption = new PolylineOptions().color(0xAA0000FF).width(
                                10).points(tempLatLngList);
                        mBaiduMap.addOverlay(polylineOption);
                    }

                    break;
                case constant.FLAG_FIRST_LOCATION:
                    showLocOnFirst(trackLocation.getBdLocation());
                    break;
                case constant.FLAG_UI:
                    double mDistance = intent.getDoubleExtra(constant.KEY_DISTANCE, 0.0D);
                    mSpeed = intent.getDoubleExtra(constant.KEY_SPEED, 0.0D);
                    int mKal = intent.getIntExtra(constant.KEY_KAL, 0);
                    tv_cal.setText(mKal + "");
                    tv_distance.setText(Double.toString(mDistance));
                    tv_speed.setText(Double.toString(mSpeed));
                    break;
                default:
                    break;
            }

        }
    };
}
