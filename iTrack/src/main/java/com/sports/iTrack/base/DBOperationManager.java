//package com.sports.iTrack.base;
//
//import android.app.Activity;
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import com.sports.iTrack.model.RecordPoint;
//import com.sports.iTrack.model.TrackItem;
//
//import java.util.ArrayList;
//
///**
// * 实现对行车轨迹记录的增删改查
// * Created by aaron_lu on 2/4/15.
// */
//public class DBOperationManager {
//
//    private final Activity activity;
//
//    public DBOperationManager(Activity activity){
//        this.activity = activity;
//    }
//
//
//
//    public void addRecordPoint(RecordPoint recordPoint){
//        ContentValues values = new ContentValues();
////        values.put(DBHelper.RecordPointDBHelper.ID_COL, recordPoint.getId());
//        values.put(DBHelper.RecordPointDBHelper.SPEED_COL, recordPoint.getSpeed());
//        values.put(DBHelper.RecordPointDBHelper.ALTITUDE_COL, recordPoint.getAltitude());
//        values.put(DBHelper.RecordPointDBHelper.DISTANCE_COL, recordPoint.getDistance());
//        values.put(DBHelper.RecordPointDBHelper.LATITUDE_COL, recordPoint.getLatitude());
//        values.put(DBHelper.RecordPointDBHelper.LONGITUDE_COL, recordPoint.getLongitude());
//        values.put(DBHelper.RecordPointDBHelper.TIMESTAMP_COL, (int)System.currentTimeMillis());
////        values.put(DBHelper.RecordPointDBHelper.RECORDDETAIL_ID_COL, );
//
//        SQLiteOpenHelper helper = new DBHelper.RecordPointDBHelper(activity);
//
//        SQLiteDatabase db = null;
//        try {
//            db = helper.getWritableDatabase();
//            db.insert(DBHelper.RecordPointDBHelper.TABLE_NAME, null, values);
//        } finally {
//            close(null, db);
//        }
//    }
//    public void addTrackItem(TrackItem trackItem){
//        ContentValues values = new ContentValues();
////        values.put(DBHelper.TrackDBHelper.ID_COL, trackItem.getId());
//        values.put(DBHelper.TrackDBHelper.AVG_SPEED_COL, trackItem.getAvgSpeed());
//        values.put(DBHelper.TrackDBHelper.DESCRIPTION_COL, trackItem.getDiscription());
//        values.put(DBHelper.TrackDBHelper.DISTANCE_COL, trackItem.getDistance());
//        values.put(DBHelper.TrackDBHelper.END_TIME_COL, trackItem.getEndTime());
//        values.put(DBHelper.TrackDBHelper.MAX_ALTITUDE_COL, trackItem.getMaxAltitude());
//        values.put(DBHelper.TrackDBHelper.MAX_SPEED_COL, trackItem.getMaxSpeed());
//        values.put(DBHelper.TrackDBHelper.MIN_ALTITUDE_COL, trackItem.getMinAltitude());
//        values.put(DBHelper.TrackDBHelper.MIN_SPEED_COL, trackItem.getMinSpeed());
//        values.put(DBHelper.TrackDBHelper.RECORDPOINTS_COUNT_COL, trackItem.getRecordPointsCount());
//        values.put(DBHelper.TrackDBHelper.START_TIME_COL, trackItem.getStartTime());
//        values.put(DBHelper.TrackDBHelper.SPORT_TYPE_COL, trackItem.getSportTpye());
//        values.put(DBHelper.TrackDBHelper.TIMESTAMP_COL, System.currentTimeMillis());
//
//        SQLiteOpenHelper helper = new DBHelper.TrackDBHelper(activity);
//
//        SQLiteDatabase db = null;
//        try {
//            db = helper.getWritableDatabase();
//            db.insert(DBHelper.TrackDBHelper.TABLE_NAME, null, values);
//        } finally {
//            close(null, db);
//        }
//
//    }
//
//    private String[] COLUMNS = {
//            DBHelper.RecordPointDBHelper.DISTANCE_COL,
//            DBHelper.RecordPointDBHelper.SPEED_COL,
//            DBHelper.RecordPointDBHelper.ALTITUDE_COL,
//            DBHelper.RecordPointDBHelper.LATITUDE_COL,
//            DBHelper.RecordPointDBHelper.LONGITUDE_COL,
//            DBHelper.RecordPointDBHelper.TIMESTAMP_COL,
//    };
//    public ArrayList<RecordPoint> getRecordPointList() {
//        //查询数据库，得到所有的RecordPoint
//        ArrayList<RecordPoint> items = new ArrayList<RecordPoint>();
//        SQLiteDatabase db = null;
//        Cursor cursor = null;
//        SQLiteOpenHelper helper = new DBHelper.RecordPointDBHelper(activity);
//        try {
//            db = helper.getReadableDatabase();
//            cursor = db.query(DBHelper.RecordPointDBHelper.TABLE_NAME, COLUMNS, null, null, null,
//                    null, DBHelper.RecordPointDBHelper.TIMESTAMP_COL + " DESC");
//            while (cursor.moveToNext()) {
//                int id = cursor.getInt(0);
//                String distance = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.RecordPointDBHelper.DISTANCE_COL));
//                float speed = cursor.getFloat(cursor.getColumnIndexOrThrow(DBHelper.RecordPointDBHelper.SPEED_COL));
//                double altitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.RecordPointDBHelper.ALTITUDE_COL));
//                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.RecordPointDBHelper.LATITUDE_COL));
//                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.RecordPointDBHelper.LONGITUDE_COL));
//                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RecordPointDBHelper.TIMESTAMP_COL));
////                RecordPoint recordPoint = new RecordPoint(distance, speed, altitude, latitude, longitude, timestamp);
////                items.add(recordPoint);
//            }
//        } finally {
//            close(cursor, db);
//        }
//
//        return items;
//    }
//
//    private static final String[] ID_COL_PROJECTION = { DBHelper.RecordPointDBHelper.ID_COL };
//    public void deleteRecordPoint(int number){
//        SQLiteOpenHelper helper = new DBHelper.RecordPointDBHelper(activity);
//        SQLiteDatabase db = null;
//        Cursor cursor = null;
//        try {
//            db = helper.getWritableDatabase();
//            cursor = db.query(DBHelper.RecordPointDBHelper.TABLE_NAME,
//                    ID_COL_PROJECTION, null, null, null, null,
//                    DBHelper.RecordPointDBHelper.TIMESTAMP_COL + " DESC");
//            cursor.move(number + 1);
//            db.delete(DBHelper.RecordPointDBHelper.TABLE_NAME, DBHelper.RecordPointDBHelper.ID_COL +
//                    '=' + cursor.getString(0), null);
//        } finally {
//            close(cursor, db);
//        }
//    }
//
//    public void deleteTrackItem(int id){
//        SQLiteOpenHelper helper = new DBHelper.RecordPointDBHelper(activity);
//        SQLiteDatabase db = null;
//        try {
//            db = helper.getWritableDatabase();
//            db.delete(DBHelper.TrackDBHelper.TABLE_NAME, "WHERE _id=" + id, null);
//        } finally {
//            close(null, db);
//        }
//    }
//
//
//
//    private static void close(Cursor cursor, SQLiteDatabase database) {
//        if (cursor != null) {
//            cursor.close();
//        }
//        if (database != null) {
//            database.close();
//        }
//    }
//}
