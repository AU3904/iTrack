package com.sports.iTrack.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aaron_lu on 2/10/15.
 */
public class TimeUtil {

    public final static String HH_MM_SS = "HH:mm:ss";
    public final static String YYYY_MM_DD = "yyyy-MM-dd HH:mm";
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 差1天8个小时，TODO why ?
     * @param timestamp
     * @param pattern
     * @return
     */
    public static String formatTimestamp(long timestamp, String pattern){
        SimpleDateFormat sdf=new SimpleDateFormat(pattern);
        String sd = sdf.format(new Date(timestamp));
        return sd;
    }

    /**
     * 得到时间间隔，同时转化为可理解时间:时分秒
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getTimeSpan(long startTime, long endTime) {
        long h = 0;
        long m = 0;
        long s = 0;
        try {
            long between = (endTime - startTime) / 1000;//ms -> s
            //d = between / (24 * 3600);
            h = between / 3600;
            m = between % 3600 / 60;
            s = between % 60;
        } catch (Exception e) {
        }

        return h + "时" + m + "分" + s + "秒";
    }

    /**
     * 保留两位小数点
     * @param data
     * @return
     */
    public static double formatData(double data) {
        double temp = data;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        temp = Double.parseDouble(df.format(temp));
        return temp;
    }

    /**
     * 用于格式化线路规划时，查询到的时间
     * @param time
     * @return
     */
    public static String formatTime(int time) {
        if (time == 0)
            return "0";
        int N = time / 3600;
        time = time % 3600;
        int K = time / 60;
        time = time % 60;
        int M = time;
        return N + "时 " + K + "分 ";
    }

//    /**
//     * 用于格式化线路规划查询的
//     * @param distance
//     * @return
//     */
//    public static String formatDistance(int distance) {
//        if (distance == 0)
//            return "0";
//        DecimalFormat decimalFormat = new DecimalFormat(".0");
//        return decimalFormat.format(distance);
//    }

    /**
     * 卡路里计算公式，
     * 先获取平均速度，根据平均速度，来得到 每30min 消耗的卡路里 值
     *
     * 卡路里计算公式： kal = 单位值 * time/30 * kg/60
     *
     */
    public static int getKal(double avgSpeed, int duration, int kg) {

        if (duration == 0 || kg == 0) {
            return -1;
        }

        int unitConsume = 0;
        if (avgSpeed < 19) {
            unitConsume = 202;
        } else if (avgSpeed > 19 && avgSpeed < 25) {
            unitConsume = 282;
        } else if (avgSpeed > 25) {
            unitConsume = 443;
        }

        int kal = unitConsume * (duration / 30) * (kg / 60);

        return kal;
    }

}
