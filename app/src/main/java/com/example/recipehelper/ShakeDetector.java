package com.example.recipehelper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int TIME_BTWN_SHAKES_MILLIS = 500;

    public interface Listener {
        void onShake();
    }
    private Listener mOnShakeListener;

    private long mShakeTimestamp;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(mOnShakeListener != null) {
            float gForce = 0f;
            for(int i = 0; i < event.values.length; i++) {
                float g = event.values[i] / SensorManager.GRAVITY_EARTH;
                gForce += (g * g);
            }
            gForce = (float)Math.sqrt(gForce);
            if(gForce > SHAKE_THRESHOLD_GRAVITY){
                final long currentTime = System.currentTimeMillis();
                if(mShakeTimestamp +  TIME_BTWN_SHAKES_MILLIS > currentTime){
                    return;
                }
                mShakeTimestamp = currentTime;
                mOnShakeListener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void setOnShakeListener(Listener onShakeListener) {
        mOnShakeListener = onShakeListener;
    }

    public void removeOnShakeListener() {
        mOnShakeListener = null;
    }
}
