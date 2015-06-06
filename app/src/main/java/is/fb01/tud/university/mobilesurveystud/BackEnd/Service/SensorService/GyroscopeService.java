package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService;

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
public class GyroscopeService extends SensorDetectorServiceBase implements SensorEventListener{

    static final public String TAG = "GyroService";

    private SensorManager mSensorManager;
    private Sensor mGyroscope;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public final void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, mGyroscope, GlobalSettings.gGyroEventDelay );

        mLastUpdate = System.currentTimeMillis();
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

        checkActivity(GlobalSettings.gGyroEventWait, GlobalSettings.gGyroThreshold);

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

        mDetectedSensorSum += normDeltaX + normDeltaY + normDeltaZ;// - (Math.PI * 3);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /*private void checkForBroadcast(){
        if(mMotionSumm > GlobalSettings.gGyroThreshold){
            isMoving = true;
            sendBroadcast(isMoving);
        }else if (isMoving) {
            isMoving = false;
            sendBroadcast(isMoving);
        }
        //else do nothing here

        Log.v(TAG, "isMoving: " + isMoving);
    }*/


    private float[] absMatrixDiff(float[] m1, float[]m2){
        int matrixSize = m1.length;

        float[] toReturn = new float[matrixSize];

        for(int i = 0; i < matrixSize; i++) {
            toReturn[i] = Math.abs(m1[i] - m2[i]);
        }
        return toReturn;
    }

}
