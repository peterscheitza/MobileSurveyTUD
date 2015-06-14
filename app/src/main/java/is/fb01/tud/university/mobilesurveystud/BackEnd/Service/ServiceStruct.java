package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.content.Intent;

/**
 * Created by peter_000 on 04.06.2015.
 */
public class ServiceStruct {

    static public enum start {
        SCREEN_ON, SCREEN_OFF, ADDITIONAL
    }

    static public enum stop {
        NEVER, SCREEN_OFF, INACTIVITY
    }

    ServiceStruct(Intent intent, start start, stop stop){
        mIntent = intent;
        mStart = start;
        mStop = stop;
        //type = eType;
    }

    public Intent mIntent;
    start mStart;
    stop mStop;

    //public MainService.ServiceType type;
    public MainService.State state = MainService.State.UNDEFINED;
    public boolean isActive = false;
}
