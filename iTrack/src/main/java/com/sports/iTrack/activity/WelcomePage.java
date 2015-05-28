package com.sports.iTrack.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import com.sports.iTrack.R;

import java.util.List;

public class WelcomePage extends Activity implements View.OnClickListener{
    private final static String KEY_START_METHOD = "KEY_START_METHOD";
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        findViewById(R.id.bt_login).setOnClickListener(this);
        findViewById(R.id.bt_route_plan).setOnClickListener(this);
        findViewById(R.id.bt_track).setOnClickListener(this);
        findViewById(R.id.bt_history).setOnClickListener(this);
        findViewById(R.id.bt_music).setOnClickListener(this);
        findViewById(R.id.bt_about).setOnClickListener(this);
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.bt_login) {

        } else if (v.getId() == R.id.bt_track ) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(KEY_START_METHOD, "way_track");
            startActivity(intent);
        } else if (v.getId() == R.id.bt_route_plan ) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(KEY_START_METHOD, "way_route_plan");
            startActivity(intent);
        }else if (v.getId() == R.id.bt_history ) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        }else if (v.getId() == R.id.bt_music ) {
            openApp("com.tencent.qqmusic");
        }else if (v.getId() == R.id.bt_weather ) {

        }else if (v.getId() == R.id.bt_about ) {

        }
    }


    private void openApp(String pm) {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(pm, 0);

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = getPackageManager().queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                String packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                ComponentName cn = new ComponentName(packageName, className);

                intent.setComponent(cn);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

