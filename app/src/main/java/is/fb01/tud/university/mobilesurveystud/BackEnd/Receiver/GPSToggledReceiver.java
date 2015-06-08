package is.fb01.tud.university.mobilesurveystud.BackEnd.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Created by peter_000 on 08.06.2015.
 */
public class GPSToggledReceiver extends BroadcastReceiver implements GpsStatus.Listener {

    static final public String TAG = "GPSToggledReceiver";

    final private String ACTION = "android.location.PROVIDERS_CHANGED";

    private Context mContext;
    private Intent mMainService;

    LocationManager mLocationManager;

    private MainService.State mMainServiceState;
    private boolean mGPSEnabled;
    private boolean mIsListening = false;

    /*GPSToggledReceiver(){
        Log.v(TAG, "init GPS receiver");

        mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        mGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        handleGPSReceive();
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v(TAG, "onReceive");

        if (intent.getAction().matches(ACTION)) {

            mContext = context;
            mMainService = new Intent(mContext, MainService.class);

            String shardPrefName = mContext.getString(R.string.shared_Pref);
            String optioneName = mContext.getString(R.string.is_active);

            SharedPreferences sharedPref = mContext.getSharedPreferences(shardPrefName, Context.MODE_PRIVATE);
            String sMainServiceState = sharedPref.getString(optioneName, MainService.State.UNDEFINED.toString());
            mMainServiceState = MainService.State.valueOf(sMainServiceState);


            mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            mGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            handleGPSReceive();
        }
    }

    private void handleGPSReceive() {
        if (mGPSEnabled) {
            if(!mIsListening) { //only on listener
                Log.v(TAG, "add listener");
                mLocationManager.addGpsStatusListener(this);
                mIsListening = true;
            }
        } else {
            if(mIsListening) {
                Log.v(TAG, "remove listener");
                mLocationManager.removeGpsStatusListener(this);
                mIsListening = false;
            }
        }
    }

    //private class GPSListener implements GpsStatus.Listener {

        @Override
        public void onGpsStatusChanged(int event) {
            Log.v(TAG, "onGpsStatusChanged: " + event);

            switch(event) {
                case GpsStatus.GPS_EVENT_STOPPED: startMain(); break;

                case GpsStatus.GPS_EVENT_STARTED: stopMain(); break;
            }
        }

        private void startMain(){
            if(mMainServiceState == MainService.State.ON) {
                Log.v(TAG, "startService");
                mContext.startService(mMainService);
            }
        }

        private void stopMain(){
            if(mMainServiceState == MainService.State.ON) {
                Log.v(TAG, "stopService");
                mContext.stopService(mMainService);
            }
        }
    //}


}
