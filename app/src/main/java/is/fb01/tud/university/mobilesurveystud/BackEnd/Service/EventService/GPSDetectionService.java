package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 08.06.2015.
 * Erfasst ob der Nutzer das GPS seines Gerätes nutzt. Hierzu wird kontrolliert ob
 * Events an GpsSatusListener geschickt werden. Sollte dies der Fall sein hat eine
 * Anwednung der Ortung via GPS gefordert.
 *
 * Ziel des GPSDetectionService ist es, eine Lokalisation des Gerätes mittels GPS zu erkennen. In diesem Fall ist zu vermuten, dass der
 * Nutzer sein Smartphone als Navigationsgerät benutzt. Diese Form der Nutzung soll von Mobile Survey TUD ebenfalls als Aktivität
 * wahrgenommen werden, weil es sich um eine Verwendung des Gerätes handelt. Dieser Aspekt ist außerdem sicherheitstechnisch relevant,
 * da der Nutzer während der Navigation im Auto nicht dazu aufgefordert werden soll, das Smartphone zu bedienen. Dies könnte zu einem
 * Verkehrsunfall führen, was vermieden werden muss. Zur Ermittlung der Aktivität des GPS-Chips wird in der OnCreate()-Methode des
 * Service zunächst geprüft, ob der Nutzer die GPS-Ortung in den Einstellungen erlaubt.  Ist dies nicht der Fall, beendet sich der
 * Service selbstständig und meldet Inaktivität. Da in modernen Smartphones jedoch immer energiesparsamere GPS-Chips eingebaut werden,
 * ist eine dauerhafte Standortermittlung nicht unüblich. Der Chip befinden sich in dieser Zeit im Ruhemodus. In diesem Fall meldet sich
 * der GPSDetectionService als Listener beim Systemdienst LocationManager  an. Hierzu implementiert der Service das Interface
 * GpsStatus.Listener  und die dazugehörige Methode onGpsStatusChanged(..). Die Methode wird aufgerufen, wenn sich etwas hinsichtlich
 * der GPS-Verbindung verändert hat. Dies kann der Beginn einer Verbindungsherstellung oder das Vorliegen neuer Satellitendaten sein.
 * Bei zweckmäßiger Verwendung dieser Funktionalität sollte eine Anwendung eine Verbindung beantragen und bei Empfang eines
 * GPS_EVENT_SATELLITE_STATUS vom Location-Manager die aktuellen Daten anfordern. Mobile Survey TUD hingegen prüft nur,
 * ob eine andere Anwendung eine Lokalisation durchführt. In diesem Fall empfängt der GPSDetection-Service ebenfalls die obigen Events
 * und interpretiert dies als Aktivität. Dies hat zu Folge, dass die App nicht die Position des Nutzers, sondern nur deren Erfassung
 * durch eine andere Anwendung ermittelt.
 */
public class GPSDetectionService extends EventDetectorServiceBase implements GpsStatus.Listener {

    static final public String TAG = "GPSDetector";

    LocationManager mLocationManager;

    /**
     *
     * @return Identifikator des Detektor
     */
    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Inizialisierung des LocationManagers und prüfen ob GPS vom Nutzer überhaupt freigegeben wurde
     * Ist dies nicht der Fall wird Inaktivität angenommen
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean GPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(GPSEnabled) {
            Log.v(TAG, "init GPS listener");
            mLocationManager.addGpsStatusListener(this);
        } else {
            //inactivity - save energy
            stopSelf();
        }

    }

    /**
     *
     * @return Dauer nach der Inaktivität angenommen wird
     */
    @Override
    long getWaitTime() {
        return GlobalSettings.gGPSEventWait;
    }

    /**
     * Entfernen des Listeners vor dem Beenden
     */
    @Override
    public void onDestroy() {
        mLocationManager.removeGpsStatusListener(this);
        super.onDestroy();
    }


    /**
     * Funktion die vom Listeneraufgerufen wird wenn ein neue Satelitendaten vorliegen
     * @param event Art des Events
     */
    @Override
    public void onGpsStatusChanged(int event) {
        Log.v(TAG, "onGpsStatusChanged: " + event);

        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            onEvent();
        }
    }
}
