package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 08.06.2015.
 */
public class GPSDetectionService extends EventDetectorServiceBase implements GpsStatus.Listener {

    static final public String TAG = "GPSDetector";

    LocationManager mLocationManager;


    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean GPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(GPSEnabled) {
            Log.v(TAG, "init GPS listener");
            mLocationManager.addGpsStatusListener(this);
        }
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeGpsStatusListener(this);
    }



    @Override
    public void onGpsStatusChanged(int event) {
        Log.v(TAG, "onGpsStatusChanged: " + event);

        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            onEvent(GlobalSettings.gGPSEventWait);
        }
    }
}
