package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 08.06.2015.
 */
public class PhoneDetectionService extends EventDetectorServiceBase {

    static final public String TAG = "PhoneDetector";

    private TelephonyManager mTeleManager;
    private Handler mCheckTeleHandler;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTeleManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        mCheckTeleHandler = new Handler();
        Runnable mEventRunnable = new Runnable(){
            public void run() {
                if (mTeleManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    Log.v(TAG, "still phoning");
                    onEvent(GlobalSettings.gPhoneEventWait);
                    mCheckTeleHandler.postDelayed(this, GlobalSettings.gPhoneRequestWait);
                }
                else {
                    Log.v(TAG, "call finished");
                }
            }
        };
        mCheckTeleHandler.postDelayed(mEventRunnable, GlobalSettings.gPhoneRequestWait);
    }

    @Override
    public void onDestroy() {

        mCheckTeleHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

}
