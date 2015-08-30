package is.fb01.tud.university.mobilesurveystud.BackEnd.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling.ServiceStruct;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Created by peter_000 on 29.08.2015.
 */
public class DetectorReceiver extends BroadcastReceiver {

    static final String TAG = "DetectorReceiver";

    private MainService mMainService;
    private boolean mIsExtendedRunning = false;

    String mSender, mServiceType;
    boolean mIsActive;
    long mCurrentStart, mCurrentEnd;


    public DetectorReceiver(MainService mainService){
        mMainService = mainService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        readDate(intent);

        Log.v(TAG, "onReceiveMessage from Service: " + mSender + " mIsActive: " + mIsActive);

        updateMainStartEnd(mCurrentStart, mCurrentEnd);

        updateMainServiceMap(mSender, mIsActive);

        updateExtendedDetectors();

        if(!mIsActive)
            mMainService.isShowADialog();

        //TODO fünf eigene funktionen
    }

    void readDate(Intent intent){
        mSender = intent.getStringExtra(mMainService.getString(R.string.sender));
        mServiceType = intent.getStringExtra(mMainService.getString(R.string.serviceType));
        mIsActive = intent.getBooleanExtra(mMainService.getString(R.string.is_active), false);
        mCurrentStart = intent.getLongExtra(mMainService.getString(R.string.millsStart), -1);
        mCurrentEnd = intent.getLongExtra(mMainService.getString(R.string.millsEnd), -1);
    }

    private void updateMainStartEnd(long millsStart, long millsEnd){
        if(millsStart > -1)
            if (mMainService.mMillsStart == -1 || millsStart < mMainService.mMillsStart)
                mMainService.mMillsStart = millsStart;

        if(millsEnd > -1)
            if (mMainService.mMillsEnd < millsEnd)
                mMainService. mMillsEnd = millsEnd;
    }

    private void updateMainServiceMap(String sender, boolean isActive){
        if (MainService.mServiceMap.containsKey(sender)) {
            ServiceStruct struct = MainService.mServiceMap.get(sender);

            struct.isActive = isActive;
            if (struct.checkStop(ServiceStruct.stopSituation.INACTIVITY) && !isActive) {
                mMainService.stopService(struct.mIntent);
                struct.state = GlobalSettings.State.OFF;
            }
        } else {
            Log.v(TAG, "something went wrong. service not found");
            assert false;
        }
    }

    private void updateExtendedDetectors(){
        if (isStandardInactivity()) {
            if (!mIsExtendedRunning) {
                mMainService.startServiceEvent(ServiceStruct.startSituation.EXTEND);
                mIsExtendedRunning = true;
            }
        }

        boolean isE = isExtendedInactivity();
        boolean isS = isStandardInactivity();
        if(isE && !isS)
            mIsExtendedRunning = false;
    }


    //-----HELPER-----
    private boolean isStandardInactivity(){
        for ( ServiceStruct serviceStruct : MainService.mServiceMap.values()) {
            if(serviceStruct.state == GlobalSettings.State.ON && !serviceStruct.checkStart(ServiceStruct.startSituation.EXTEND)) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    private boolean isExtendedInactivity(){
        for ( ServiceStruct serviceStruct : MainService.mServiceMap.values()) {
            if(serviceStruct.state == GlobalSettings.State.ON && serviceStruct.checkStart(ServiceStruct.startSituation.EXTEND)) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }
}
