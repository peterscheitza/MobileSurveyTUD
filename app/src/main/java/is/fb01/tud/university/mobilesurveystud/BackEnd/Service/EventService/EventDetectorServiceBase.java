package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;

/**
 * Created by peter_000 on 04.06.2015.
 */
public abstract class EventDetectorServiceBase extends DetectorServiceBase {

    static final public String SERVICETAG = "EventDetector";

    private Handler mEventHandler;
    private Runnable mEventRunnable;

    @Override
    public String getServiceType() {
        return SERVICETAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mEventHandler = new Handler();
        mEventRunnable = new Runnable(){
            public void run() {
                isActive = false;
                sendBroadcast(getTag());
                resetParamter();
            }
        };
    }

    @Override
    public void onDestroy() {
        mEventHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    protected void onEvent() {
        if (isActive) {
            mEventHandler.removeCallbacksAndMessages(null);
            mEventHandler.postDelayed(mEventRunnable, GlobalSettings.gTouchEventWait);
            mMillsEnd = System.currentTimeMillis();
        } else {
            Log.v(getTag(), "starting to detect some events");
            mEventHandler.postDelayed(mEventRunnable, GlobalSettings.gTouchEventWait);
            mMillsStart = System.currentTimeMillis();
            mMillsEnd = System.currentTimeMillis();
            isActive = true;
            sendBroadcast(getTag());
        }
    }
}
