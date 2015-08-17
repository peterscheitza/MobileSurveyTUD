package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.EventDetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 08.06.2015.
 */
public class PhoneDetectionService extends HandlerDetectorServiceBase {

    static final public String TAG = "PhoneDetector";

    private TelephonyManager mTeleManager;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTeleManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    long getHandlerDelay() {
        return GlobalSettings.gPhoneRequestWait;
    }

    @Override
    boolean conditionToCheck() {
        return mTeleManager.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }

}
