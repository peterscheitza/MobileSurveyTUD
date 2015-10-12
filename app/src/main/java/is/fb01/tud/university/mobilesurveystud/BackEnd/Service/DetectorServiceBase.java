package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.FrontEnd.Notifier;
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Created by peter_000 on 04.06.2015.
 * Basis aller Detektoren. Kümmer sich um das Speichern essenzieller Daten
 * und der Kommunikation mit dem Rest der Anwednung. Zusätzlich wird der für
 * Der String für das Intent der Übertragung definiert und statisch zugänglich gemacht.
 *
 * Detektoren sind Ableitungen von Android-Services, deren Aufgabe es ist, die zugewiesene Interaktion zu erkennen. Hierzu werden sie,
 * wie bereits im Kapitel „Verwaltung der Detektoren mittels ServiceStructs“ erwähnt, nach einem festen Regelwerk gestartet. Zur
 * Laufzeit ist das Vorgehen bei jedem Detektor gleich:
 *
 * 1.	Sollte ein Detektor den Beginn einer Interaktion erfassen, meldet er es dem MainService, damit sich dieser einer
 *      Nutzerinteraktion bewusst ist.
 *
 * 2.	Daraufhin kontrolliert der Dienst in regelmäßigen Abständen, ob der Nutzer noch aktiv ist. Dies geschieht je nach
 *      Erfassungsart auf unterschiedliche Weise und wird später in diesem Abschnitt erläutert.
 *
 * 3.	Nachdem der Detektor keine Aktivität mehr erfasst, meldet er dies wieder dem MainService. Hierbei teilt er ihm
 *      auch den Zeitpunkt der letzten erfassten Aktivität mit.
 *
 * Für den modularen Aufbau der Anwendung sorgt die dreischichtige Vererbungsstruktur bis zur eigentlichen Erfassung der
 * Nutzerinteraktion. Die für die oben beschriebenen Basisaufgaben benötigten Funktionen sind in der DetektorServiceBase gebündelt,
 * von der sich alle spezifischeren Detektorenbasen ableiten.
 */
public abstract class DetectorServiceBase extends Service {

    public static final String Package = "is.fb01.tud.university.mobilesurveystud";
    public static final String MSG = Package + ".MSG";

   // Dies ist jetzt die Aufgabe der ServiceStructs
   // protected boolean mServiceStopSelf = true;

    protected long mMillsStart = -1;
    protected long mMillsEnd = -1;
    protected boolean isActive = false;

    private LocalBroadcastManager mBroadcaster;

    /**
     * Soll von dem eigentlich Detektor implementiert werden und dient der Identifikation
     * @return eindeutiger Name des Detektor
     */
    public abstract String getTag();

    /**
     * Soll von der Zwischenklasse implementiert werden und dient der Identifikation
     * @return eindeutiger Name der Detektorengattung
     */
    public abstract String getServiceType();

    /**
     * Der Service wird sofort nach dem Start mit der Priorität Foreground versehen, damit er nicht vom
     * System automatisch beendet werden darf
     *
     * Zusätzlich werden die Parameter initzialisiert und der BroadcastManager local zu Kommunikation erzeugt
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(getTag(), "onServiceConnected");

        Notification notification  = new Notifier(this).getForgroundNotification();
        startForeground(0,notification);

        resetParamter();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Legt fest das der Service im falle eines ungeplanten schließens wieder gesataret wird
     *
     * @param intent
     * @param flags
     * @param startId
     * @return Konstat die Verhalten festlegt
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Sollte der Service beendet werden meldet er ggf inaktivität, da er diese dann nicht mehr erfassen kann
     *
     * Zusätzlich wird die Forground Flag aufgehoben
     */
    @Override
    public void onDestroy() {
        Log.v(getTag(), "onServiceDisconnected");

        //secures that mainService now that this service is not active it get killed
        //the if clause is important: if you fire two isActive=false vasts it is possible to get in an endless loop
        // -> first stop -> reset extended flag -> second stop -> restart

        if(isActive) {
            isActive = false; //assume inactivity when off
            sendBroadcast();
        }

        stopForeground(false);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Sendet eine Benachrichtigung an den MainService/DetektoreReceiver und informiert ihn über den aktuellen
     * Aktivität des Nutzers und den letzen Start- und Endzeitpunkt der Kommunikation (sofern bekannt)
     */
    protected void sendBroadcast(){
        Log.v(getTag(), "Broadcasting handler message");

        Intent intent = new Intent("Stopped receiving accessibility events");
        intent.setAction(MSG);
        intent.putExtra(getString(R.string.sender), getTag());
        intent.putExtra(getString(R.string.serviceType), getServiceType());
        intent.putExtra(getString(R.string.millsStart), mMillsStart);
        intent.putExtra(getString(R.string.millsEnd), mMillsEnd);
        intent.putExtra(getString(R.string.is_active), isActive);
        mBroadcaster.sendBroadcast(intent);
    };

    /**
     * Setzt alle wichtigen Member betreffend der Aktivität zurück
     */
    protected void resetParamter(){
        isActive = false;
        mMillsStart = -1;
        mMillsEnd = -1;
    }
}
