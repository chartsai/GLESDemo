package idv.chatea.gldemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import idv.chatea.gldemo.firework.GLFireworkSurfaceView;

/**
 * Created by chatea on 2016/2/1.
 * @author Charlie Tsai (chatea)
 */
public class GLES2_Firework_Activity extends Activity implements SensorEventListener {

    private static final String TAG = GLES2_Firework_Activity.class.getSimpleName();

    /**
     * How sensitive of shake detection.
     */
    private static final int SHAKE_THRESHOLD = 900;

    private GLFireworkSurfaceView mGLSurfaceView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private Vibrator mVibrator;

    private long mLastUpdate;
    private float mPreviousX;
    private float mPreviousY;
    private float mPreviousZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLFireworkSurfaceView(this);
        setContentView(mGLSurfaceView);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null) {
            mAccelerometer = sensor;
        } else {
            Log.e(TAG, "This device doesn't support accelerometer");
        }

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - mLastUpdate) > 100) {
                long diffTime = (currentTime - mLastUpdate);
                mLastUpdate = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x+y+z - mPreviousX - mPreviousY - mPreviousZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d(TAG, "shake detected... speed: " + speed);
                    if (mVibrator != null) {
                        mVibrator.vibrate((long) speed);
                    }
                    mGLSurfaceView.handleShakeEvent(speed);
                }
                mPreviousX = x;
                mPreviousY = y;
                mPreviousZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing.
    }
}
