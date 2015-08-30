package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 04.06.2015.
 */
public class ServiceStruct {

    static public enum startSituation {
        SCREEN_ON, SCREEN_OFF, EXTEND
    }

    static public enum stopSituation {
        NEVER, SCREEN_OFF, INACTIVITY
    }

    public ServiceStruct(Intent intent, startSituation start, stopSituation stop){
        initCollection();

        mIntent = intent;
        mStartList.add(start);
        mStopList.add(stop);
    }

    public ServiceStruct(Intent intent, List<startSituation> start, stopSituation stop){
        initCollection();

        mIntent = intent;
        mStartList = start;
        mStopList.add(stop);
    }

    public ServiceStruct(Intent intent, startSituation start,  List<stopSituation> stop){
        initCollection();

        mIntent = intent;
        mStartList.add(start);
        mStopList = stop;
    }

    public ServiceStruct(Intent intent,  List<startSituation> start,  List<stopSituation> stop){
        initCollection();

        mIntent = intent;
        mStartList = start;
        mStopList = stop;
    }

    public Intent mIntent;
    List<startSituation> mStartList;
    List<stopSituation> mStopList;
    DetectorServiceBase mDetector;

    //public MainService.ServiceType type;
    public GlobalSettings.State state = GlobalSettings.State.UNDEFINED;
    public boolean isActive = false;

    private void initCollection(){
        mStartList = new ArrayList<startSituation>();
        mStopList = new ArrayList<stopSituation>();
    }
    
    public boolean checkStop(stopSituation stop){

        for (stopSituation s : mStopList){
            if(s == stop)
                return true;
        }

        return false;
    }

    public boolean checkStart(startSituation start){

        for (startSituation s : mStartList){
            if(s == start)
                return true;
        }

        return false;
    }
}
