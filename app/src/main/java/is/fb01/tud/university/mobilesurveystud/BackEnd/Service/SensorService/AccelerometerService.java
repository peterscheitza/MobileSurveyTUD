package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 25.05.2015.
 */
public class AccelerometerService extends SensorDetectorServiceBase implements SensorEventListener {

    static final public String TAG = "AccelService";

    private SensorManager mSensorManager;
    private Sensor mAcceleromter;

    private double mLastX = 0.0f;
    private double mLastY = 0.0f;
    private double mLastZ = 0.0f;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public final void onCreate() {
        super.onCreate();

        Log.v(TAG, "onServiceConnected");

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mAcceleromter = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this,mAcceleromter,GlobalSettings.gAccelEventDelay);
    }


    @Override
    public void onDestroy() {

        mSensorManager.unregisterListener(this);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        checkActivity(GlobalSettings.gAccelEventWait, GlobalSettings.gAccelThreshold);

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double xAbs = Math.toDegrees(Math.asin(x/SensorManager.GRAVITY_EARTH));
        double yAbs = Math.toDegrees(Math.asin(y/SensorManager.GRAVITY_EARTH));
        double zAbs = Math.toDegrees(Math.asin(z/SensorManager.GRAVITY_EARTH));

        double movedX = Math.abs(xAbs - mLastX);
        double movedY = Math.abs(yAbs - mLastY);
        double movedZ = Math.abs(zAbs - mLastZ);

        mDetectedSensorSum += movedX + movedY + movedZ;

        mLastX = xAbs;
        mLastY = yAbs;
        mLastZ = zAbs;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
