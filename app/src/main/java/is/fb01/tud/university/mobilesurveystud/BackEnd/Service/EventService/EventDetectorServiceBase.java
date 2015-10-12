package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;

/**
 * Created by peter_000 on 04.06.2015.
 * Basis der Detektoren die inaktivität annehmen, wenn  ein Event
 * in einem festen Zeitraum nicht mehr aufgerufen wurde.
 *
 * Ziel der EventDetectorServiceBase ist es, die verschiedenen Events zu erfassen und den MainService bei deren Ausbleiben zu
 * informieren. Hierzu registrieren die abgeleiteten Klassen einen Listener, der auf Systemevents achtet, sowie die Funktion
 * onEvent() der EventDetectorServiceBase. Wird diese Funktion nicht mehr nach einem in den GlobalSettings vorgegebenen Intervall
 * aufgerufen, wird Inaktivität angenommen.
 */
public abstract class EventDetectorServiceBase extends DetectorServiceBase {

    static final public String SERVICETAG = "EventDetector";

    private Handler mEventHandler;
    private Runnable mEventRunnable;

    /**
     * Muss von der Base ausgeprägt werden
     *
     * @return String der die Service Basis identifiziert
     */
    @Override
    public String getServiceType() {
        return SERVICETAG;
    }

    /**
     * Erzeugen und Starten eines Handlers.
     *
     * Dieser Handler meldet Inaktivität mittels Broadcast an den MainService/DetectorReceiver
     * Der Handler wird beim Aufruf der Funktion onEvent wieder zurückgesetzt und es wird weiterhin
     * Inaktivität angenommen.
     *
     * Die Laufzeit des Handlers wird due die ableitende Klasse mittels getWaitTime festgelegt
     *
     * Der Handler muss in der onCreate Initzial gesartete werden um den MainService über ein ausbleiben der
     * Aktivität zu informieren
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mEventHandler = new Handler();
        mEventRunnable = new Runnable(){
            public void run() {
                isActive = false;

                sendBroadcast();
                resetParamter();
            }
        };

        mEventHandler.postDelayed(mEventRunnable, getWaitTime());
    }

    /**
     * Diese Funktion muss von einer ableitendne Klasse definiert werden
     *
     * @return Dauer nach der, der Detektor Inaktivität annimmt
     */
    abstract long getWaitTime();

    @Override
    public void onDestroy() {
        mEventHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    /**
     * Sollte Aktivität vorliegen wird der aktuelle Handler entfernt und damit nicht ausgeführt.
     * Danach wird ein neuer Handler mit der vorgegebenen Zeit gestartet
     *
     * Sollte Inaktivität vorliegen wird ebenfalls ein Handler gesatrtet und zustäzlich der Startzeitpunkt
     * der aktuellen Aktivität erfasst
     */
    protected void onEvent() {
        if (isActive) {
            mEventHandler.removeCallbacksAndMessages(null);
            mEventHandler.postDelayed(mEventRunnable,  getWaitTime());
            mMillsEnd = System.currentTimeMillis();
        } else {
            Log.v(getTag(), "starting to detect some events");
            mEventHandler.postDelayed(mEventRunnable,  getWaitTime());
            mMillsStart = System.currentTimeMillis();
            mMillsEnd = System.currentTimeMillis();
            isActive = true;
            sendBroadcast();
        }
    }
}
