package is.fb01.tud.university.mobilesurveystud;

import android.app.Service;
        import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorEventListener2;
        import android.hardware.SensorManager;
        import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
        import android.widget.Toast;

/**
 * Created by peter_000 on 25.05.2015.
 */
public class GyroscopeService extends Service implements SensorEventListener{

    static final public String TAG = "GyroService";
    static final public String MSG = "is.fb01.tud.university.mobilesurveystud." + TAG + ".MSG";

    LocalBroadcastManager mBroadcaster;

    private SensorManager mSensorManager;
    private Sensor mAcceleromter;

    private long mLastUpdate;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    private double mMotionSumm = 0.00f;
    private boolean isMoving = false;

    @Override
    public final void onCreate() {
        super.onCreate();

        Log.v(TAG, "onServiceConnected");

        mBroadcaster  = LocalBroadcastManager.getInstance(this);

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mAcceleromter = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this,mAcceleromter,SensorManager.SENSOR_DELAY_GAME);
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

        checkActivity();

        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > 0.00001) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;

        double deltaX = Math.atan2(deltaRotationMatrix[7],deltaRotationMatrix[8]);
        double deltaY = Math.atan2(- deltaRotationMatrix[6], Math.sqrt(Math.pow(deltaRotationMatrix[7],2) + Math.pow(deltaRotationMatrix[8],2)));
        double deltaZ = Math.atan2(deltaRotationMatrix[3],deltaRotationMatrix[0]);

        double normDeltaX = Math.abs(deltaX);
        double normDeltaY = Math.abs(deltaY * 2);
        double normDeltaZ = Math.abs(deltaZ);

        mMotionSumm += normDeltaX + normDeltaY + normDeltaZ;// - (Math.PI * 3);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void checkActivity(){
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - mLastUpdate);

        if (diffTime > GlobalSettings.gGyroEventWait) {
            checkForBroadcast();

            mLastUpdate = curTime;
            Log.v(TAG, "" + mMotionSumm);
            mMotionSumm = 0.0f;
        }
    }

    private void checkForBroadcast(){
        if(mMotionSumm > GlobalSettings.gGyroThreshold){
            isMoving = true;
            sendBroadcast(isMoving);
        }else if (isMoving) {
            isMoving = false;
            sendBroadcast(isMoving);
        }
        //else do nothing here

        Log.v(TAG, "isMoving: " + isMoving);
    }

    private void sendBroadcast(boolean movement){
        Log.v(TAG, "Broadcasting handler message");

        Intent intent = new Intent("high device movement");
        intent.setAction(MSG);
        intent.putExtra("movementDetected", movement);
        mBroadcaster.sendBroadcast(intent);
    }

    private float[] absMatrixDiff(float[] m1, float[]m2){
        int matrixSize = m1.length;

        float[] toReturn = new float[matrixSize];

        for(int i = 0; i < matrixSize; i++) {
            toReturn[i] = Math.abs(m1[i] - m2[i]);
        }
        return toReturn;
    }

}
