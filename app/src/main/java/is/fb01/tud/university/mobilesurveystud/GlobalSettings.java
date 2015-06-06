package is.fb01.tud.university.mobilesurveystud;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    final public static int gTouchEventWait   = 30000; // 30 Seconds
    final public static int gGyroEventWait   = 30000; // 30 Seconds
    final public static int gGyroThreshold = 30; //need to be adjusted when gGyroEventChanges
    final public static int gMinUseDuration    = 30000; // 2min

    final public static int gSoundRequest = 10000; //needs to be smaller then gTouchEventWait

    final public static String gDialogHead = "Dialog head: Hello asshole";
    final public static String gDialogBody = "Dialog body: I think you are inactive go and answer the survey you useless bitch";
    final public static String gDialogGoToButton = "Go to survey";
    final public static String gDialogExistButton = "Not now";

    final public static String gSurveyURL = "http://www.golem.de"; //needs to start with "http://" or "https://"
}
