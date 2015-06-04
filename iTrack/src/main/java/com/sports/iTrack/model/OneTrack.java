package com.sports.iTrack.model;

public class OneTrack {
    private static OneTrack ourInstance;

    public static OneTrack getInstance() {
        if (ourInstance == null) {
            ourInstance = new OneTrack();
        }
        return ourInstance;
    }

    private OneTrack() {
    }

    public boolean isPause() {
        return pause;
    }

    public void pause() {
        this.pause = true;
    }

    public void resume() {
        this.pause = false;
    }

    public boolean isStart() {
        return start;
    }

    public void start() {
        this.start = true;
    }

    public void end() {
        this.start = false;
    }

    public boolean isFirstLocation() {
        return firstLocation;
    }

    public void setFirstLocation(boolean firstLocation) {
        this.firstLocation = firstLocation;
    }

    public void destroy() {
        ourInstance = null;
    }

    private boolean pause = false;
    private boolean firstLocation = true;
    private boolean start = false;
}
