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

    /**
     * gMinUseDuration
     * Der hier angegebene Wert gibt in Millisekunden vor, wie lange die Interaktion mit dem Gerät gewesen
     * sein muss, damit das Anzeigen des Dialoges in Betracht gezogen wird. Er ist standardmäßig auf 15 Sekunden (15*1000) eingestellt.
     */
    final public static long gMinUseDuration    = 15*1000; //should be more then 1

    /**
     * gMaxShowCounter und gResetShowCounter
     * Diese Variablen geben an, wie oft die Aufforderung zum Ausfüllen der Umfrage
     * in einem fixen Zeitraum maximal angezeigt werden darf. Hierzu wird bei jeder Aufforderung ein Zähler erhöht und beim
     * Überschreiten von gMaxShowCounter werden keine Dialoge mehr angezeigt. Dabei wird unter gResetShowCounter mittels
     * Millisekunden angegeben, in welchem Intervall der Zähler der angezeigten Dialoge zurückgesetzt wird. Der Wert von
     * gMaxShowCounter ist zehn und der von gResetShowCounter beträgt 24 Stunden (24*60*60*1000). Mehr Details zum Zurücksetzen
     * des ShowCoutners sind unter dem Kapitel „ShowCounter zurücksetzen“ angegeben.
     */
    final public static long gResetShowCounter = 24*60*60*1000; //time in mills
    final public static long gMaxShowCounter = 10;

    /**
     * gIdleAfterShow
     * Hier kann in Millisekunden angegeben werden, wie lange die Erfassung der Nutzerinteraktion pausiert
     * werden soll, nachdem der Nutzer die Umfrage vorgelegt bekam. Hierdurch wird auch das Anzeigen des Dialoges für
     * diesen Zeitraum verhindert. Somit gibt der Wert dieser Variable vor, wie lange der Umfrageteilnehmer nach dem Ausfüllen
     * der Befragung geschont wird und senkt den Energieverbrauch der Anwendung. Standardmäßig ist der Wert auf 30 Minuten (30*60*1000)
     * eingestellt.
     */
    final public static long gIdleAfterShow = 1000 * 60 * 30;

    /**
     * gPercentageToShow
     * Der Wert dieser Variable muss zwischen 1 und 100 liegen und gibt an, wie viel Prozent der Nutzerinteraktionen zu einem
     * tatsächlichen Anzeigen des Dialoges führen (Siehe Kapitel „Anzeigen des Dialoges“). Diese Variable enthält standardmäßig
     * den Wert 20. Das heißt, im Schnitt wird bei jeder fünften Interaktion zur Umfrage aufgefordert.
     */
    final public static int gPercentageToShow = 100; //between 1 and 100

    /**
     * gSurveyURL und gGetURLWithID
     * In gSurveyURL wird die Webadresse der Umfrage angegeben. Wichtig hierbei ist, dass dem Uniform Resource Locater (kurz URL)
     * über ein Präfix wie “http://” oder “https://” das Transferprotokoll vorangestellt wird. Vom MainService wird jediglich die
     * Funktion gGetURLWithID aufgerufen, in der die Nutzeridentifikation mit der Basis-URL kombiniert wird. Diese dient der
     * Zuordnung des Studienteilnehmers zu einer Umfrage. Sollten mehr Parameter als nur die Identifikation in der URL übergeben werden,
     * müssen diese in die Funktion gGetURLWithID eingefügt werden.
     */
    public static String gSurveyURL = "http://www.golem.de/"; //needs to start with "http://" or "https://"
    public static String gGetURLWithID(Context c){
        // There is no sense in saveing the UID in the MainService due the idle time of Main after the showing
        // of the survey
        SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_Pref),c.MODE_PRIVATE);
        String sUserID = sp.getString(c.getString(R.string.user_id),"");

        return gSurveyURL + sUserID + "/";
    }

    /**
     * gIsShowWebView
     * Sollte der Nutzer den Bildschirm des Gerätes verdunkeln, kann in dem dann erscheinenden Dialogfenster ein kleines
     * Browserfeld geöffnet werden, insofern zuvor Aktivität festgestellt wurde. In diesem Feld kann der Nutzer sofort die
     * Umfrage beantworten, ohne sein Gerät zu entsperren. Da sich dieses Feature als uninteressant herausgestellt hat, wird es
     * standardmäßig deaktiviert, indem gIsShowWebView auf false gesetzt wird.
     */
    final public static boolean gIsShowWebView = false;

    /**
     * gMinIdleHours und gMaxIdleHours
     * Im Front-End kann der Nutzer mittels eines Einstellrades auswählen, wie viele Stunden die Anwendung pausiert werden soll
     * (siehe Kapitel „Ruhemodus und Pausieren“). Die Werte, die in dem Rad wählbar sind, liegen linear zwischen den beiden hier
     * angegebenen Variablen. Es versteht sich, dass gMaxIdleHours (8) größer als gMinIdleHours(2) sein muss.
     */
    final public static int gMinIdleHours = 2;
    final public static int gMaxIdleHours = 8;

    /**
     * gDefaultMainService
     * Dieser Status gibt mittels der Enumeration MainService.State.ON oder MainService. State.OFF an, ob der MainService und
     * damit die Nutzerdetektion sofort nach der Installation der Anwendung gestartet werden soll. Die Erfassung ist standardmäßig
     * aktiv.
     */
    final public static State gDefaultMainService = State.ON;




    //-------------------------------------------------------------------------
    //---------------------------ADDITIONAL SETTINGS---------------------------
    //-------------------------------------------------------------------------

    /**
     * Diese Werte geben an, wie viele Millisekunden die verschiedenen Detektoren auf ein Event
     * warten sollen. Die jeweiligen Events sind bei jedem Detektor unterschiedlich und daher muss
     * dementsprechend gewartet werden.
     */
    final public static long gTouchEventWait =  30*1000;
    final public static long gGPSEventWait = 5000;
    final public static long gSoundEventWait = 10000;
    final public static long gPhoneEventWait = 5000;
    final public static long gGyroEventWait = 30000;
    final public static long gAccelEventWait = 30000;

    /**
     * Hier wird in Mikrosekunden angegeben, in welchem Intervall das System den jeweiligen Sensor
     * auslesen soll.
     */
    final public static int gGyroEventDelay = 200000; // microseconds (!) -> 200 milliseconds
    final public static int gAccelEventDelay = 200000; // microseconds (!) -> 200 milliseconds

    /**
     * Sollten die mittels Sensoren berechneten Werte diese Grenzwerte überschreiten, wird
     * Nutzeraktivität angenommen.
     */
    final public static int gGyroThreshold = 10; //need to be adjusted when gGyroEventWait or gGyroEventDelay changes
    final public static int gAccelThreshold = 200; //need to be adjusted when gAccelEventWait or gAccelEventDelay changes

    /**
     * Eine Liste an Apps, die nicht im Vordergrund laufen dürfen, wenn ein Dialog angezeigt
     * werden soll.
     */
    final public static String[]gDefaultExceptionalApps = {
            "com.google.android.apps.maps"
    };

    /**
     * Das Zeitintervall das der MainService nach einem außerplanmäßigen Beenden berücksichtigen
     * soll, bis er versucht, sich wieder neu zu starten.
     */
    final public static long gTryToRestartMain = 30000;

    /**
     * Zeit nach der die DiaolgActivity auf dem Sperrbildschirm automatisch entfernt wird
     */
    final public static long gDismissDialog = 2 *60*1000;


    static public enum State {
        ON,OFF,UNDEFINED
    }
}
