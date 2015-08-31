package is.fb01.tud.university.mobilesurveystud;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    //-------------------------------------------------------------------------
    //----------------------------STANDARD SETTINGS----------------------------
    //-------------------------------------------------------------------------
    final public static long gMinUseDuration    = 15*1000; //15 sec

    final public static long gResetShowCounter = 24*60*60*1000; //time in mills
    final public static long gMaxShowCounter = 10;
    final public static long gIdleAfterShow = 1000 * 60 * 30;

    final public static int gPercentageToShow = 20; //between 1 and 100

    public static String gSurveyURL = "http://www.golem.de/"; //needs to start with "http://" or "https://"
    public static String gGetURLWithID(Context c){
        // There is no sense in saveing the UID in the MainService due the idle time of Main after the showing
        // of the survey
        SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_Pref),c.MODE_PRIVATE);
        String sUserID = sp.getString(c.getString(R.string.user_id),"");

        return gSurveyURL + sUserID + "/";
    }

    final public static boolean gIsShowWebView = false;

    final public static int gMinIdleHours = 1;
    final public static int gMaxIdleHours = 8;

    final public static State gDefaultMainService = State.ON;




    //-------------------------------------------------------------------------
    //---------------------------ADDITIONAL SETTINGS---------------------------
    //-------------------------------------------------------------------------
    final public static long gTouchEventWait =  30*1000;

    final public static long gGPSEventWait = 5000;


    final public static long gSoundEventWait = 10000;

    final public static long gPhoneEventWait = 5000;


    final public static long gGyroEventWait = 30000;
    final public static int gGyroEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gGyroThreshold = 10; //need to be adjusted when gGyroEventWait or gGyroEventDelay changes

    final public static long gAccelEventWait = 30000;
    final public static int gAccelEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gAccelThreshold = 200; //need to be adjusted when gAccelEventWait or gAccelEventDelay changes

    final public static String[]gDefaultExceptionalApps = {
            "com.google.android.apps.maps"
    };

    final public static long gTryToRestartMain = 30000;

    static public enum State {
        ON,OFF,UNDEFINED
    }
}
