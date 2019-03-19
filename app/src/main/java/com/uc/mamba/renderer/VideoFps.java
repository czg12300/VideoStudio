package com.uc.mamba.renderer;

import android.util.Log;

public class VideoFps {

    long lastTimes = 0;
    int count = 0;

    /**
     * 打印fps
     */
    public void printFps() {
        long now = System.currentTimeMillis();
        if (lastTimes > 0) {
            if (now - lastTimes >= 1000) {
                Log.d("tag", "CameraRenderer fps:" + count);
                lastTimes = now;
                count = 0;
            } else {
                count++;
            }
        } else {
            lastTimes = now;
        }
    }
}
