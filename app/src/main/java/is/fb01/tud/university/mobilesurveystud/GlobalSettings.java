package is.fb01.tud.university.mobilesurveystud;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    final public static int gEventWait   = 30*1000; // 30 Seconds
    final public static int gMinUseDuration    = 15*1000; //15 sec

    final public static int gResetShowCounter = 5*60*1000; //time in mills
    final public static int gMaxShowCounter = 999;
    final public static int gIdleAfterShow = 1000 * 60; //* 30;

    final public static int gPercentageToShow = 100;

    final public static String gDialogHead = "Dialog head: Hello";
    final public static String gDialogBody = "Dialog body: I think you are inactive go and answer the survey";
    final public static String gDialogGoToButton = "Go to survey";
    final public static String gDialogExistButton = "Not now";

    public static String gSurveyURL = "http://www.golem.de/specials/android-5.="; //needs to start with "http://" or "https://"
    public static String gGetURLWithID(Context c){
        SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_Pref),c.MODE_PRIVATE);
        String sUserID = sp.getString(c.getString(R.string.user_id),"");

        return gSurveyURL + sUserID;
    }





    //Additional Settings:
    final public static int gTouchEventWait = gEventWait;

    final public static int gGPSEventWait = 5000;

    final public static int gSoundEventWait = gEventWait; //could be smaller //buttonDetection //deprecated!!!!


    final public static int gSoundRequestWait = 10000;

    final public static int gPhoneRequestWait = 5000;


    final public static int gGyroEventWait = 30000;
    final public static int gGyroEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gGyroThreshold = 10; //need to be adjusted when gGyroEventWait or gGyroEventDelay changes

    final public static int gAccelEventWait = 30000;
    final public static int gAccelEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gAccelThreshold = 200; //need to be adjusted when gAccelEventWait or gAccelEventDelay changes

    final public static String[]gDefaultExceptionalApps = {
            "com.google.android.apps.maps"
    };


    final public static int gTryToRestartMain = 30000;

    final public static boolean gIsShowWebView = false;
}
