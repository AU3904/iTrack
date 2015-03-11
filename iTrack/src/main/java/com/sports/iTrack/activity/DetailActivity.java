package com.sports.iTrack.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.sports.iTrack.R;
import com.sports.iTrack.model.RecordPoint;
import com.sports.iTrack.model.TrackItem;
import com.sports.iTrack.utils.TimeUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by aaron_lu on 2/10/15.
 */
public class DetailActivity extends FragmentActivity implements View.OnClickListener {

    public final static String ID_TRACK_ITEM = "id_track_item";

    private static TrackItem mCurrentTrackItem;
    private static List<RecordPoint> mPoints = new ArrayList<RecordPoint>();
    private static List<LatLng> mLatLngs = new ArrayList<LatLng>();

    private static List<Double> mLatitudeArray = new ArrayList<Double>();
    private static List<Double> mLongitudeArray = new ArrayList<Double>();

    private RelativeLayout mTabBtnAbout;
    private RelativeLayout mTabBtnDetail;
    private RelativeLayout mTabBtnMap;
    private FragmentPagerAdapter mAdapter;
    private ViewPager mViewPager;
    private List<Fragment> mFragments = new ArrayList<Fragment>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long id = getIntent().getLongExtra(ID_TRACK_ITEM, -1);
        new DataGetTask().execute(id);

        /**
         * 0. 初始化界面
         * 1. getIntent 从 historyActivity 中获得数据
         * 2. 在onClick 方法中填充数据
         */
        setContentView(R.layout.detail_track_layout);
        initView();
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mPoints.clear();
        mLatLngs.clear();
        mLatitudeArray.clear();
        mLongitudeArray.clear();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_tab_bottom_about:
                mViewPager.setCurrentItem(0, true);
                break;
            case R.id.id_tab_bottom_detail:
                mViewPager.setCurrentItem(1, true);
                break;
            case R.id.id_tab_bottom_map:
                mViewPager.setCurrentItem(2, true);
                break;
            default:
                mViewPager.setCurrentItem(0, true);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DataGetTask extends AsyncTask<Long, Void, Void> {

        @Override protected Void doInBackground(Long... params) {
            List<TrackItem> trackItemList = DataSupport.where("id = ?",
                    String.valueOf(params[0])).find(
                    TrackItem.class);

            if (trackItemList.size() < 1) {
                return null;
            }

            mCurrentTrackItem = trackItemList.get(0);

            mPoints = DataSupport.where("trackitem_id = ?",
                    String.valueOf(params[0])).find(RecordPoint.class);
            /**
             * 构建经纬度数据
             */
            for (RecordPoint recordPoint : mPoints) {
                mLatLngs.add(new LatLng(recordPoint.getLatitude(), recordPoint.getLongitude()));
                mLatitudeArray.add(recordPoint.getLatitude());
                mLongitudeArray.add(recordPoint.getLongitude());
            }


            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

/*    LineChart[] mCharts = new LineChart[1]; // 4条数据
    //    Typeface mTf; // 自定义显示字体
    int[] mColors = new int[] { Color.rgb(137, 230, 81), Color.rgb(240, 240, 30),//
            Color.rgb(89, 199, 250), Color.rgb(250, 104, 104) }; // 自定义颜色

    String[] mMonths = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };*/

    /**
     * 初始化所有的textview
     */
    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        mTabBtnAbout = (RelativeLayout) findViewById(R.id.id_tab_bottom_about);
        mTabBtnDetail = (RelativeLayout) findViewById(R.id.id_tab_bottom_detail);
        mTabBtnMap = (RelativeLayout) findViewById(R.id.id_tab_bottom_map);

        mTabBtnAbout.setOnClickListener(this);
        mTabBtnDetail.setOnClickListener(this);
        mTabBtnMap.setOnClickListener(this);

        AboutRecordFragment tab01 = new AboutRecordFragment();
        DetailRecordFragment tab02 = new DetailRecordFragment();
        MapRecordFragment tab03 = new MapRecordFragment();
        mFragments.add(tab01);
        mFragments.add(tab02);
        mFragments.add(tab03);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mFragments.get(arg0);
            }
        };

        mViewPager.setAdapter(mAdapter);

        /**
         * 设置第一次进去的效果；
         */
        mViewPager.setCurrentItem(0);
        ((ImageView) mTabBtnAbout.findViewById(R.id.iv_tab_about))
                .setImageResource(R.drawable.postselected);
        ((ImageView) mTabBtnAbout.findViewById(
                R.id.iv_tab_about_bottom)).setVisibility(
                View.VISIBLE);


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int currentIndex;

            @Override
            public void onPageSelected(int position) {
                resetTabBtn();
                switch (position) {
                    case 0:
                        ((ImageView) mTabBtnAbout.findViewById(R.id.iv_tab_about))
                                .setImageResource(R.drawable.postselected);
                        ((ImageView) mTabBtnAbout.findViewById(
                                R.id.iv_tab_about_bottom)).setVisibility(
                                View.VISIBLE);
                        break;
                    case 1:
                        ((ImageView) mTabBtnDetail.findViewById(R.id.iv_tab_detail))
                                .setImageResource(R.drawable.recordselected);
                        ((ImageView) mTabBtnDetail.findViewById(
                                R.id.iv_tab_detail_bottom)).setVisibility(
                                View.VISIBLE);
                        break;
                    case 2:
                        ((ImageView) mTabBtnMap.findViewById(R.id.iv_tab_map))
                                .setImageResource(R.drawable.mapselected);
                        ((ImageView) mTabBtnMap.findViewById(R.id.iv_tab_map_bottom)).setVisibility(
                                View.VISIBLE);
                        break;
                    default:
                        break;
                }

                currentIndex = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        getActionBar().setBackgroundDrawable(
                this.getBaseContext().getResources().getDrawable(R.drawable.BackBar));
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    protected void resetTabBtn() {
        ((ImageView) mTabBtnAbout.findViewById(R.id.iv_tab_about))
                .setImageResource(R.drawable.postnormal);
        ((ImageView) mTabBtnDetail.findViewById(R.id.iv_tab_detail))
                .setImageResource(R.drawable.recordnormal);
        ((ImageView) mTabBtnMap.findViewById(R.id.iv_tab_map))
                .setImageResource(R.drawable.mapnormal);

        ((ImageView) mTabBtnAbout.findViewById(R.id.iv_tab_about_bottom)).setVisibility(
                View.INVISIBLE);
        ((ImageView) mTabBtnDetail.findViewById(R.id.iv_tab_detail_bottom)).setVisibility(
                View.INVISIBLE);
        ((ImageView) mTabBtnMap.findViewById(R.id.iv_tab_map_bottom)).setVisibility(View.INVISIBLE);
    }

    public static class AboutRecordFragment extends Fragment {

        private TextView tvEndTime;
        private TextView tvDistance;
        private TextView tvAvgSpeed;
        private TextView tvAltitudeRange;
        private TextView tvSpeedRange;
        private TextView tvDesciption;
        private TextView tvSportTime;
        private TextView tvKal;

        private View recordLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            recordLayout = inflater.inflate(R.layout.about_record_layout, container, false);
            return recordLayout;
        }

        @Override public void onStart() {
            super.onStart();
            tvAltitudeRange = (TextView) recordLayout.findViewById(R.id.tv_altitude_range);
            tvAvgSpeed = (TextView) recordLayout.findViewById(R.id.tv_avg_speed);
            tvDesciption = (TextView) recordLayout.findViewById(R.id.tv_description);
            tvDistance = (TextView) recordLayout.findViewById(R.id.tv_distance);
            tvEndTime = (TextView) recordLayout.findViewById(R.id.tv_endtime);
            tvSpeedRange = (TextView) recordLayout.findViewById(R.id.tv_speed_range);
            tvSportTime = (TextView) recordLayout.findViewById(R.id.tv_sport_time);
            tvKal = (TextView) recordLayout.findViewById(R.id.tv_cal_title);
        }

        @Override public void onResume() {
            super.onResume();
            if (mCurrentTrackItem == null)
                return;
            tvEndTime.setText(
                    TimeUtils.formatTimestamp(mCurrentTrackItem.getTimestamp(),
                            TimeUtils.YYYY_MM_DD));
            tvSportTime.setText(TimeUtils.formatTime((int) mCurrentTrackItem.getDuration()));

            String str_distance = "";
            if (mCurrentTrackItem.getDistance() > 1000) {
                str_distance = TimeUtils.formatData(mCurrentTrackItem.getDistance() / 1000) + "km";
            } else {
                str_distance = TimeUtils.formatData(mCurrentTrackItem.getDistance()) + "m";
            }
            tvDistance.setText(str_distance);

            tvSpeedRange.setText(TimeUtils.formatData(mCurrentTrackItem.getMaxSpeed()) + "km/h");
            tvAltitudeRange.setText(
                    mCurrentTrackItem.getMinAltitude() + "m/" + mCurrentTrackItem.getMaxAltitude()
                            + "m");
            tvDesciption.setText(mCurrentTrackItem.getDiscription());

            tvAvgSpeed.setText(Double.toString(mCurrentTrackItem.getAvgSpeed()) + "km/h");

            tvKal.setText(Integer.toString(mCurrentTrackItem.getKal()) + "大卡");
        }
    }

    public static class DetailRecordFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.detail_record_layout, container, false);
        }

        @Override public void onStart() {
            super.onStart();
            //            Intent intent = new Intent(getActivity(), TestActivity.class);
            //            startActivity(intent);
        }
    }

    public static class MapRecordFragment extends Fragment {

        private View mapRecordLayout;
        private MapView mMapView;
        private BaiduMap mBaiduMap;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mapRecordLayout = inflater.inflate(R.layout.map_record_layout, container, false);
            return mapRecordLayout;
        }

        @Override public void onStart() {
            super.onStart();

            /**
             * 初始化mapview 和 baiduMap，不启动定位
             *
             */
            mMapView = (MapView) mapRecordLayout.findViewById(R.id.bmapView);
            mBaiduMap = mMapView.getMap();

            if (mLatLngs == null || mLatLngs.size() < 2 || mPoints == null || mPoints.size() == 0) {
                return;
            }
            mBaiduMap.clear();

            //start point
            LatLng ll = new LatLng(mPoints.get(0).getLatitude(),
                    mPoints.get(0).getLongitude());

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

            OverlayOptions polygonOption = new PolylineOptions().color(0xAA0000FF).width(
                    10).points(mLatLngs);
            mBaiduMap.addOverlay(polygonOption);


            mBaiduMap.setMapStatus(
                    MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(getZoom()).build()));

        }

        @Override public void onResume() {
            super.onResume();
            mMapView.onResume();
        }

        @Override public void onPause() {
            super.onPause();
            mMapView.onPause();
        }

        @Override public void onDestroy() {
            super.onDestroy();
            mMapView.onDestroy();
        }
    }

    /**
     * 得到活动的最大经度，和最小经度；
     * 计算最小维度，不同经度间的距离，得到a
     *
     * 得到活动的最大维度，和最小维度
     * 计算相同经度，不同维度间的距离，得到b
     *
     * 比较a与b，得到较大值c
     * 将c 与百度地图 比例尺 尺寸比较，设置合理放大尺寸
     */

    private static int getZoom(){

        Collections.sort(mLatitudeArray);
        Collections.sort(mLongitudeArray);
        double maxLong = mLongitudeArray.get(mLongitudeArray.size() - 1);
        double minLong = mLongitudeArray.get(0);

        double maxLat = mLatitudeArray.get(mLatitudeArray.size() - 1);
        double minLat = mLatitudeArray.get(0);


        double a = DistanceUtil.getDistance(new LatLng(minLat, maxLong), new LatLng(minLat, minLong));
        double b = DistanceUtil.getDistance(new LatLng(maxLat, maxLong), new LatLng(minLat, maxLong));

        int c = (int) Math.max(a, b);

        int zoom = 0;
        if (c < 20) {
            zoom = 19;
        } else if (c > 20 && c < 200) {
            zoom = 19;
        } else if (c > 200 && c < 2000) {
            zoom = 16;
        } else if (c > 2000 && c < 20000) {
            zoom = 13;
        } else if (c > 20000 && c < 100000) {
            zoom = 10;
        } else if (c > 100000 && c < 500000) {
            zoom = 7;
        } else if (c > 500000 && c < 1000000) {
            zoom = 5;
        } else if (c > 1000000) {
            zoom = 4;
        }
        return zoom;
    }
}
