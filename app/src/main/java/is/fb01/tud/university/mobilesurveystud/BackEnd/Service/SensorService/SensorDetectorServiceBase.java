package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService;

import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;

/**
 * Created by peter_000 on 04.06.2015.
 */
public abstract class SensorDetectorServiceBase extends DetectorServiceBase {

    static final public String SERVICETAG = "SensorDetector";

    protected long mLastUpdate;
    protected double mDetectedSensorSum;

    @Override
    public String getServiceType() {
        return SERVICETAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isActive = false; //we assume no activity
        mLastUpdate = System.currentTimeMillis();
    }

    /*protected void sendBroadcast(String TAG){
        super.sendBroadcast(TAG);
    }*/

    protected boolean checkActivity(int iEventWait, int iThreshold){
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - mLastUpdate);

        if (diffTime > iEventWait) {

            if(mDetectedSensorSum > iThreshold && !isActive) {
                isActive = true;
                mMillsStart = curTime;
                mMillsEnd = curTime;
                sendBroadcast(getTag());
            }
            else if (mDetectedSensorSum > iThreshold && isActive) {
                mMillsEnd = curTime;
            }
            else if (mDetectedSensorSum < iThreshold && isActive) {
                isActive = false;
                sendBroadcast(getTag());
                resetParamter();
            }
            else if (mDetectedSensorSum < iThreshold && !isActive) {
                sendBroadcast(getTag());
                stopSelf();
            }

            Log.v(getTag(), "" + mDetectedSensorSum);

            mDetectedSensorSum = 0.0f;
            mLastUpdate = curTime;
        }

        return isActive;
    }

    @Override
    protected void resetParamter(){
        super.resetParamter();
        mDetectedSensorSum = 0.0f;
    }
}
