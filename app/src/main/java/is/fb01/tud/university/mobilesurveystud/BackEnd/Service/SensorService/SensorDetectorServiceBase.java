package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService;

import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;

/**
 * Created by peter_000 on 04.06.2015.
 * Basis der Detektoren, die einen Sensor des Systemsauslesen und
 * die ermittelten Daten regelmäßig mit einem Schwellwert vergleichen.
 *
 * Die SensorDetectorServiceBase befasst sich mit dem Auslesen der Gerätesensoren wie dem Gyroskop und dem Accelerometer.
 * Um die von den Sensoren ermittelten Werte in regelmäßigen Abständen mit einem Schwellwert zu vergleichen, bietet sie die
 * Funktion checkActivity(..) an. Sollte der voreingestellte Schwellwert überschritten werden, wird Aktivität angenommen.
 * Diese Werte wurden für die Sensoren durch Ausprobieren ermittelt und sollten mit Bedacht angepasst werden. Die entsprechende
 * Einstellung erfolgt in den GlobalSettings.
 */
public abstract class SensorDetectorServiceBase extends DetectorServiceBase {

    static final public String SERVICETAG = "SensorDetector";

    protected long mLastUpdate;
    protected double mDetectedSensorSum;

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
     * Festlegung der letzen Kontrolle des Schwellwertes auf den aktuellen Zeitpunkt
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mLastUpdate = System.currentTimeMillis();
    }

    /**
     * Zunächst wird ermittelt, ob seit der letzten Kontrolle genug Zeitvergangen ist
     * Sollte dies der Fall sein können Vier szenarien eintreten
     *
     * S1: Schwellwert überschritten und es liegt der Beginn einer Aktivität vor
     *      Setzen aller Paramter und Informieren des MainService/DetektorReceiver
     *
     * S2: Schwellwert überschritten und es liegt bereits Aktivität vor
     *      "Weitersetzten" des Endzeitpunktes
     *
     * S3: Schwellwert unterschritten und es liegt bereits Aktivität vor
     *      Melden von Inaktivität an den MainService/Detektorreceiver
     *
     * S4: Schwellwert unterschritten und es liegt der Beginn einer Aktivität vor
     *      Abschaltung um Storm zu spren (Systembruch - eigentlich Aufgabe der ServiceStructs)
     *
     * @param iEventWait Dauer die gewartet werden soll bis der aktuelle Wert mit dem Grenzwert verglichen wird (Strom sparen)
     * @param iThreshold Grenzwert bei dessen überschreiten Aktivität angenommen wird
     * @return
     */
    protected boolean checkActivity(long iEventWait, long iThreshold){
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - mLastUpdate);

        if (diffTime > iEventWait) {

            if(mDetectedSensorSum > iThreshold && !isActive) {
                isActive = true;
                mMillsStart = curTime;
                mMillsEnd = curTime;
                sendBroadcast();
            }
            else if (mDetectedSensorSum > iThreshold && isActive) {
                mMillsEnd = curTime;
            }
            else if (mDetectedSensorSum < iThreshold && isActive) {
                isActive = false;
                sendBroadcast();
            }
            else if (mDetectedSensorSum < iThreshold && !isActive) {
                sendBroadcast();
                stopSelf(); //just to bes sure and save energy
            }

            Log.v(getTag(), "" + mDetectedSensorSum);

            mDetectedSensorSum = 0.0f;
            mLastUpdate = curTime;
        }

        return isActive;
    }

    /**
     * Erweiterung der Resetfunktion um einen paramter
     */
    @Override
    protected void resetParamter(){
        super.resetParamter();
        mDetectedSensorSum = 0.0f;
    }
}
