package is.fb01.tud.university.mobilesurveystud;

/**
 * Created by peter_000 on 11.05.2015.
 */
public class GlobalSettings {

    final static int gTouchEventWait   = 30000; // 30 Seconds
    final static int gGyroEventWait   = 30000; // 30 Seconds
    final static int gGyroThreshold = 30; //need to be adjusted when gGyroEventChanges
    final static int gMinUseDuration    = 30000; // 2min

    final static String gDialogHead = "Dialog head: Hello asshole";
    final static String gDialogBody = "Dialog body: I think you are inactive go and answer the survey you useless bitch";
    final static String gDialogGoToButton = "Go to survey";
    final static String gDialogExistButton = "Not now";

    final static String gSurveyURL = "http://www.golem.de"; //needs to start with "http://" or "https://"

    static enum ServiceStates {
        ON,OFF,UNDEFINED
    }
}
