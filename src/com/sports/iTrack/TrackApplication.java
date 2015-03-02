package com.sports.iTrack;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import com.baidu.mapapi.SDKInitializer;
import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by aaron_lu on 2/2/15.
 */
public class TrackApplication extends LitePalApplication {

    private List<Activity> activityList = new LinkedList<Activity>();
    private static TrackApplication instance;

    @Override public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }




//    public static TrackApplication getInstance() {
//        if (null == instance) {
//            instance = new TrackApplication();
//        }
//        return instance;
//    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // 遍历所有Activity并finish
    public void exit() {
//        for (Activity activity : activityList) {
//            activity.finish();
//        }
//        System.exit(0);
    }
}
