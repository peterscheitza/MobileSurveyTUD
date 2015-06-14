package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService;

import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 14.06.2015.
 */
public abstract class HandlerDetectorServiceBase extends DetectorServiceBase {

    static final public String SERVICETAG = "EventDetector";

    protected int mHandlerDelay;
    private Handler mCheckConditionHandler;

    @Override
    public String getServiceType() {
        return SERVICETAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCheckConditionHandler = new Handler();

        runHandler();
    }

    @Override
    public void onDestroy() {

        if(mCheckConditionHandler != null)
            mCheckConditionHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    abstract int getHandlerDelay();
    abstract boolean conditionToCheck();

    private void runHandler() {
        Runnable runner = new Runnable(){
            public void run() {
                if(conditionToCheck()) {

                    mMillsEnd = System.currentTimeMillis();

                    if(!isActive) {
                        mMillsStart = System.currentTimeMillis();
                        isActive = true;
                        sendBroadcast(getTag());
                    }
                }
                else {
                    if(isActive) {
                        isActive = false;
                        sendBroadcast(getTag());
                        //stopSelf();
                    }
                }

                mCheckConditionHandler.postDelayed(this, getHandlerDelay());
            }
        };
        mCheckConditionHandler.postDelayed(runner, getHandlerDelay());
    }
}
