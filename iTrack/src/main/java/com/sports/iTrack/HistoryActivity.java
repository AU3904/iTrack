package com.sports.iTrack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sports.iTrack.model.TrackItem;
import com.sports.iTrack.utils.TimeUtil;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron_lu on 2/5/15.
 */
public class HistoryActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private ListView listView;

    private TextView tv_total_duration;
    private TextView tv_total_times;
    private TextView tv_total_distance;

    private List<TrackItem> mTrackItems = new ArrayList<TrackItem>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_layout);

        tv_total_distance = (TextView) findViewById(R.id.tv_total_distance);
        tv_total_duration = (TextView) findViewById(R.id.tv_total_duration);
        tv_total_times = (TextView) findViewById(R.id.tv_total_times);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(this);
    }

    @Override protected void onResume() {
        super.onResume();
        mTrackItems = getTrackList();

        tv_total_times.setText(Integer.toString(mTrackItems.size()));
        double total_distance = 0.0D;
        long total_duration = 0;
        for (int i = 0; i < mTrackItems.size(); i++) {
            total_distance += mTrackItems.get(i).getDistance();
            total_duration += (mTrackItems.get(i).getEndTime() - mTrackItems.get(i).getStartTime());
        }

        String str_distance = "";
        if (total_distance > 1000) {
            str_distance = TimeUtil.formatData(total_distance / 1000) + "km";
        } else {
            str_distance = TimeUtil.formatData(total_distance) + "m";
        }

        tv_total_distance.setText(str_distance);
        tv_total_duration.setText(TimeUtil.formatTimestamp(total_duration, TimeUtil.HH_MM_SS));

        listView.setAdapter(new TrackAdapter(this));
    }


    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        //size  始终比 position 大，
        intent.putExtra(DetailActivity.ID_TRACK_ITEM, (mTrackItems.size() - position));
        startActivity(intent);
    }

    public final class ViewHolder{
        public TextView tvSportType;
        public TextView tvEndTime;
        public TextView tvDistance;
        public TextView tvTime;
    }

    private class TrackAdapter extends BaseAdapter{
        private LayoutInflater mInflater;


        public TrackAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }

        @Override public int getCount() {
            return mTrackItems.size();
        }

        @Override public Object getItem(int position) {
            return mTrackItems.get(position);
        }

        @Override public long getItemId(int position) {
            return 0;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            if (convertView == null || convertView.getTag() == null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item, null);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvDistance = (TextView) convertView.findViewById(R.id.tv_distance);
            viewHolder.tvEndTime = (TextView) convertView.findViewById(R.id.tv_endtime);
            viewHolder.tvSportType = (TextView) convertView.findViewById(R.id.tv_sporttype);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);


            TrackItem item = mTrackItems.get(position);

            String str_distance = "";
            if (item.getDistance() > 1000) {
                str_distance = TimeUtil.formatData(item.getDistance() / 1000) + "km";
            } else {
                str_distance = TimeUtil.formatData(item.getDistance()) + "m";
            }
            viewHolder.tvDistance.setText(str_distance);
            viewHolder.tvEndTime.setText(TimeUtil.formatTimestamp(item.getEndTime(),
                    TimeUtil.YYYY_MM_DD));

            if (item.getSportTpye() == 1) {
                viewHolder.tvSportType.setText("骑行:");
            } else {
                viewHolder.tvSportType.setText("?");
            }

            viewHolder.tvTime.setText(TimeUtil.getTimeSpan(item.getStartTime(), item.getEndTime()));
            return convertView;
        }
    }

    private List<TrackItem> getTrackList() {
        List<TrackItem> trackItemList = DataSupport.order("endtime desc").find(TrackItem.class);
        return trackItemList;
    }

}
