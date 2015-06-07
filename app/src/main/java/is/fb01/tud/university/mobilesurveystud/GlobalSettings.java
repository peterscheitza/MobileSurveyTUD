package is.fb01.tud.university.mobilesurveystud;

import java.util.Vector;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    final public static int gEventWait   = 30000; // 30 Seconds
    final public static int gMinUseDuration    = 30000; // 2min


    final public static String gDialogHead = "Dialog head: Hello asshole";
    final public static String gDialogBody = "Dialog body: I think you are inactive go and answer the survey you useless bitch";
    final public static String gDialogGoToButton = "Go to survey";
    final public static String gDialogExistButton = "Not now";

    final public static String gSurveyURL = "http://www.golem.de"; //needs to start with "http://" or "https://"



    //Additional Settings:
    final public static int gTouchEventWait = gEventWait;

    final public static int gSoundEventWait = gEventWait;
    final public static int gSoundRequestWait = 10000; //needs to be smaller then gTouchEventWait

    final public static int gGyroEventWait = gEventWait;
    final public static int gGyroEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gGyroThreshold = 10; //need to be adjusted when gGyroEventWait or gGyroEventDelay changes


    final public static int gAccelEventWait = gEventWait;
    final public static int gAccelEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gAccelThreshold = 100; //need to be adjusted when gAccelEventWait or gAccelEventDelay changes

    final public static String[]gDefaultExceptionalApps = {
            "com.google.android.apps.maps"
    };

}
