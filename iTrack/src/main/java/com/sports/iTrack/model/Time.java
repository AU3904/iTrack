package com.sports.iTrack.model;

public class Time{
    private static Time ourInstance;

    public static Time getInstance() {
        if (ourInstance == null) {
            ourInstance = new Time();
        }
        return ourInstance;
    }

    public void secondPlus(){
        second++;
    }

    public void minPlus(){
        min++;
    }

    public void hourPlus() {
        hour++;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getTotalSeconds(){
        return hour * 3600 + min * 60 + second;
    }

    public void reset(){
        second = 0;
        min = 0;
        hour = 0;
    }

    int second = 0;
    int min = 0;
    int hour = 0;
}
