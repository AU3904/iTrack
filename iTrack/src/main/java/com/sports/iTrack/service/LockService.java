package com.sports.iTrack.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

import com.sports.iTrack.activity.MainActivity;

public class LockService extends Service {

    private Intent zdLockIntent = null;

    private boolean startTrack = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(this.getClass().getSimpleName(), "onCreate");
        zdLockIntent = new Intent(LockService.this, MainActivity.class);
        zdLockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        this.registerReceiver(mScreenOnReceiver, mScreenOffFilter);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        startTrack = intent.getBooleanExtra("trackStartFlag", false);
        Log.e(this.getClass().getSimpleName(), "onstart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(this.getClass().getSimpleName(), "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(this.getClass().getSimpleName(), "ondestroy");
        LockService.this.unregisterReceiver(mScreenOnReceiver);
        if (startTrack) {
            startService(new Intent(LockService.this, LockService.class));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private KeyguardManager mKeyguardManager = null;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;

    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.e(this.getClass().getSimpleName(), intent.toString());

            if (action.equals("android.intent.action.SCREEN_ON")) {

                Log.e(this.getClass().getSimpleName(), "startActivity");

                mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                mKeyguardLock = mKeyguardManager.newKeyguardLock("zdLock 1");
                mKeyguardLock.disableKeyguard();
                startActivity(zdLockIntent);
            }
        }

    };

}
