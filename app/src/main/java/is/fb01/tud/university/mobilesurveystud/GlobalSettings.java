package is.fb01.tud.university.mobilesurveystud;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    final public static int gEventWait   = 30*1000; // 30 Seconds
    final public static int gMinUseDuration    = 30*1000; //

    final public static int gResetShowCounter = 5*60*1000; //time in mills
    final public static int gMaxShowCounter = 1;
    final public static int gIdleAfterShow = 30*60*1000;

    final public static int gPercentageToShow = 100;

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

    final public static int gSoundEventWait = gEventWait; //could be smaller //buttonDetection


    final public static int gSoundRequestWait = 10000;

    final public static int gPhoneRequestWait = 5000;


    final public static int gGyroEventWait = gEventWait;
    final public static int gGyroEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gGyroThreshold = 10; //need to be adjusted when gGyroEventWait or gGyroEventDelay changes

    final public static int gAccelEventWait = gEventWait;
    final public static int gAccelEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gAccelThreshold = 200; //need to be adjusted when gAccelEventWait or gAccelEventDelay changes

    final public static String[]gDefaultExceptionalApps = {
            "com.google.android.apps.maps"
    };


    final public static int gTryToRestartMain = 30000;
}
