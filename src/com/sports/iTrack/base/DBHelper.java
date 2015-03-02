//package com.sports.iTrack.base;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
///**
// * Created by aaron_lu on 2/4/15.
// */
//public class DBHelper {
//
//    //for track item db
//    static class TrackDBHelper extends SQLiteOpenHelper {
//
//        private static final int DB_VERSION = 1;
//        private static final String DB_NAME = "track_history.db";
//        static final String TABLE_NAME = "track_history";
//        static final String ID_COL = "id";
//        static final String START_TIME_COL = "start_time";
//        static final String END_TIME_COL = "end_time";
//        static final String DISTANCE_COL = "distance";
//        static final String AVG_SPEED_COL = "avg_speed";
//        static final String MAX_SPEED_COL = "max_speed";
//        static final String MIN_SPEED_COL = "min_speed";
//        static final String MAX_ALTITUDE_COL = "max_altitude";
//        static final String MIN_ALTITUDE_COL = "min_altitude";
//        static final String SPORT_TYPE_COL = "sport_type";
//        static final String RECORDPOINTS_COUNT_COL = "recordpoints_count";
//        static final String DESCRIPTION_COL = "discription";
//        static final String TIMESTAMP_COL = "timestamp";
//
//        TrackDBHelper(Context context) {
//            super(context, DB_NAME, null, DB_VERSION);
//        }
//
//        @Override public void onCreate(SQLiteDatabase db) {
//
//            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
//                            + ID_COL + " INTEGER PRIMARY KEY, "
//                            + START_TIME_COL + " INTEGER, "
//                            + END_TIME_COL + " INTEGER, "
//                            + DISTANCE_COL + " REAL, "
//                            + AVG_SPEED_COL + " REAL, "
//                            + MAX_SPEED_COL + " REAL, "
//                            + MIN_SPEED_COL + " REAL, "
//                            + MAX_ALTITUDE_COL + " DOUBLE, "
//                            + MIN_ALTITUDE_COL + " DOUBLE, "
//                            + SPORT_TYPE_COL + " INTEGER, "
//                            + RECORDPOINTS_COUNT_COL + " INTEGER, "
//                            + DESCRIPTION_COL + " TEXT, "
//                            + TIMESTAMP_COL + " TEXT);"
//            );
//        }
//
//        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//            onCreate(db);
//        }
//    }
//
//    //for record point db
//    static class RecordPointDBHelper extends SQLiteOpenHelper {
//
//        private static final int DB_VERSION = 1;
//        private static final String DB_NAME = "record_point.db";
//        static final String TABLE_NAME = "record_point";
//        static final String ID_COL = "id";
//        static final String DISTANCE_COL = "distance";
//        static final String SPEED_COL = "speed";
//        static final String ALTITUDE_COL = "altitude";
//        static final String LATITUDE_COL = "latitude";
//        static final String LONGITUDE_COL = "longitude";
//        static final String TIMESTAMP_COL = "timestamp";
//        static final String RECORDDETAIL_ID_COL = "recorddetail_id";
//
//        RecordPointDBHelper(Context context) {
//            super(context, DB_NAME, null, DB_VERSION);
//        }
//
//        @Override public void onCreate(SQLiteDatabase db) {
//
//            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
//                            + ID_COL + " INTEGER PRIMARY KEY, "
//                            + DISTANCE_COL + " REAL, "
//                            + SPEED_COL + " REAL, "
//                            + ALTITUDE_COL + " DOUBLE, "
//                            + LATITUDE_COL + " DOUBLE, "
//                            + LONGITUDE_COL + " DOUBLE, "
//                            + TIMESTAMP_COL + " INTEGER, "
//                            + RECORDDETAIL_ID_COL + " INTEGER);"
//            );
//        }
//
//        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//            onCreate(db);
//        }
//    }
//
//}
