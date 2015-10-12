package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.EventDetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 08.06.2015.
 * Dieser Service kontrolliert ob der Nutzer gerade telefoniert
 *
 * Der PhoneDetectionService prüft mittels des Systemdienstes TelephonyManger  in einem Abstand von fünf Sekunden,
 * ob ein Telefonat geführt wird. Hierzu wird der aktuelle CallState abgefragt und, sollte dieser nicht CALL_STATE_IDLE
 * sein, wird Aktivität angenommen. Wie im Kapitel “Verwaltung der Detektoren mittels ServiceStructs” erläutert, ist es wichtig,
 * dass dieser Service auch aktiv bleibt, wenn sich der Bildschirm verdunkelt.
 */
public class PhoneDetectionService extends HandlerDetectorServiceBase {

    static final public String TAG = "PhoneDetector";

    private TelephonyManager mTeleManager;

    /**
     *
     * @return Identifikator des Detektor
     */
    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Inizialisierung des TelephoneManagers
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mTeleManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * Festlegung des Intervalles indem geprüft werden soll
     * @return
     */
    @Override
    long getHandlerDelay() {
        return GlobalSettings.gPhoneEventWait;
    }

    /**
     * Prüfen ob der CallState NICHT idle ist (dann wird telefoniert)
     * @return
     */
    @Override
    boolean conditionToCheck() {
        return mTeleManager.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }

    /**
     * nicht zu beachten
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
