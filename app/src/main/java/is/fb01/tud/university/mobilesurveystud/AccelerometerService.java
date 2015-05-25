package is.fb01.tud.university.mobilesurveystud;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by peter_000 on 25.05.2015.
 */
public class AccelerometerService extends Service implements SensorEventListener{

    static final public String TAG = "AccelService";
    static final public String MSG = "is.fb01.tud.university.mobilesurveystud." + TAG + ".MSG";

    private SensorManager mSensorManager;
    private Sensor mAcceleromter;

    private long mLastUpdate;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    @Override
    public final void onCreate() {
        super.onCreate();

        Log.v(TAG, "onServiceConnected");

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mAcceleromter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this,mAcceleromter,SensorManager.SENSOR_DELAY_NORMAL);

        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v(TAG, "onServiceDisconnected");

        mSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()  == Sensor.TYPE_ACCELEROMETER) {

            long curTime = System.currentTimeMillis();
            long diffTime = (curTime - mLastUpdate);

            if (diffTime < 100)
                return;

            mLastUpdate = curTime;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;


            if (mAccel > 12) {
                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken. Level: 12", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            if (mAccel > 10) {
                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken. Level: 10", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            if (mAccel > 8) {
                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken. Level: 8", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            if (mAccel > 6) {
                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken. Level: 6", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
