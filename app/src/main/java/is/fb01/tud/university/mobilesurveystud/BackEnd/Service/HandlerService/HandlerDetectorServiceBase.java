package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService;

import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 14.06.2015.
 * Basis der Detektoren die eine feste Systemvariable prüfen sollen.
 * Um den Stromverbrauch nicht unnötig zu erhöhen wird dies nur in einem
 * vorgeben Intervall durch einen Handler getan
 *
 * Die HandlerDetectorServiceBase befasst sich mit dem regelmäßigen Auslesen von Systemwerten mittels eines Handlers. Hierzu
 * implementieren Klassen, die von der Handler-DetectorServiceBase abgeleitet sind, die Funktion conditionToCheck(). Diese Funktion
 * wird von einem Handler in regelmäßigen Abständen aufgerufen und gibt einen Boolean zurück, der angibt, ob Aktivität vorliegt.
 * Das Intervall, in dem Aktivität geprüft werden soll, wird in der abstrakten Funktion getHandlerDelay() festgelegt.
 */
public abstract class HandlerDetectorServiceBase extends DetectorServiceBase {

    static final public String SERVICETAG = "HandlerDetector";

    private Handler mCheckConditionHandler;

    /**
     * Muss von der Base ausgeprägt werden
     *
     * @return String der die Service Basis identifiziert
     */
    @Override
    public String getServiceType()
    {
       return SERVICETAG;
    }

    /**
     * Erzeugen und Starten des Handler zur Kontrolle der Bedingung
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mCheckConditionHandler = new Handler();

        runHandler();
    }

    /**
     *  Diese Funktion muss von einer ableitendne Klasse definiert werden
     *
     * @return Dauer nach der, die Bedingung geprüft werden soll
     */
    abstract long getHandlerDelay();

    /**
     * Bedingung die Geprüft werden soll
     *
     * @return liegt noch Aktivität vor?
     */
    abstract boolean conditionToCheck();

    /**
     * Abmelden des Handlers und melden von Inaktivität
     */
    @Override
    public void onDestroy() {

        if(mCheckConditionHandler != null)
            mCheckConditionHandler.removeCallbacksAndMessages(null);

        if(isActive)
        {
            isActive = false;
            sendBroadcast();
        }

        super.onDestroy();
    }

    /**
     * Starten des Handler zur Kontrolle der Vordefinierten Bedingung
     *
     * Hierzu wird geprüft ob noch Aktivtät vorliegt und dies ggf dem MainService/DetektorReceiver gemeldet
     *
     * Zusätzlich startet der Handler sich selbst wieder neu undbleibt solange der Detektor aktivit ist auch aktiv
     */
    private void runHandler() {
        Runnable runner = new Runnable(){
            public void run() {
                if(conditionToCheck()) {

                    Log.v(getTag(),"i am still true");
                    mMillsEnd = System.currentTimeMillis();

                    if(!isActive) {
                        mMillsStart = System.currentTimeMillis();
                        isActive = true;
                        sendBroadcast();
                    }
                }
                else {
                   // if(isActive) {
                        isActive = false;
                        sendBroadcast();
                        //stopSelf();
                  //  }
                }

                mCheckConditionHandler.postDelayed(this, getHandlerDelay());
            }
        };
        mCheckConditionHandler.postDelayed(runner, 1000); //first check after 1 sec - maybe direkt inaktiv
    }
}
