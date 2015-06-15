package is.fb01.tud.university.mobilesurveystud;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    final public static int gEventWait   = 30000; // 30 Seconds
    final public static int gMinUseDuration    = 30000; // 2min

    final public static int gResetShowCounter = 24*60*60*1000; //time in mills
    final public static int gMaxShowCounter = 9999999;

    final public static String gDialogHead = "Dialog head: Hello";
    final public static String gDialogBody = "Dialog body: I think you are inactive go and answer the survey";
    final public static String gDialogGoToButton = "Go to survey";
    final public static String gDialogExistButton = "Not now";

    public static String gUserId = "";
    public static String gSurveyURL = "http://www.golem.de/"; //needs to start with "http://" or "https://"
    public static String gGetURLWithID(){
        return gSurveyURL + gUserId;
    }


    //Additional Settings:
    final public static int gTouchEventWait = gEventWait;

    final public static int gGPSEventWait = gEventWait;

    final public static int gSoundEventWait = gEventWait; //could be smaller
    final public static int gSoundRequestWait = 10000; //needs to be smaller then gSoundEventWait

    final public static int gPhoneEventWait = gEventWait; //could be smaller
    final public static int gPhoneRequestWait = 10000; //needs to be smaller then gTouchEventWait

    final public static int gGyroEventWait = gEventWait;
    final public static int gGyroEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gGyroThreshold = 10; //need to be adjusted when gGyroEventWait or gGyroEventDelay changes


    final public static int gAccelEventWait = gEventWait;
    final public static int gAccelEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gAccelThreshold = 200; //need to be adjusted when gAccelEventWait or gAccelEventDelay changes

    final public static String[]gDefaultExceptionalApps = {
            "com.google.android.apps.maps"
    };
}
