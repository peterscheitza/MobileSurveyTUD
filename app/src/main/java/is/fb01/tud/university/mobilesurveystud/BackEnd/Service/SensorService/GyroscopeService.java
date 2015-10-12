package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService;

import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
        import android.os.IBinder;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 25.05.2015.
 * Ermittelt wie Stark das Gerät geneigt wird bzw. wie Stark es rotiert wird
 *
 * Der GyroscopeService implementiert einen SensorEventListener  und kann somit das geräteeigene Gyroskop auslesen. Hierzu wird der
 * Service in seiner onCreate()-Methode bei dem systemeigenen SensorManager  als Listener für das TYPE_GYROSCOPE registriert. An
 * dieser Stelle wird dem System auch die gewünschte Ausleserate übergeben, die in den zusätzlichen Einstellungen definiert wird.
 * Als zielführend hat sich ein Wert von 200.000 Mikrosekunden erwiesen (0,2 Sekunden). Dies entspricht der von der API vorgegebenen
 * Konstante SENSOR_DELAY_NORMAL und ist eine verhältnismäßig langsame Ausleserate.  Eine solche Rate wird zum Beispiel zur Erkennung
 * der Bildschirmorientierung genutzt, ist für Spiele aber zu ungenau . Durch die Verwendung einer langsamen Leserate soll der
 * Stromverbrauch so gering wie möglich gehalten werden.
 * Desweiteren implementiert der Service das Interface SensorEventListener  und kann somit die Sensor Events des Gyroskops empfangen.
 * Hierzu definiert der Service die Funktion onSensorChanged(..) des Listeners und wertet die in einem Intent  übergebenen XYZ-Werte aus.
 * Diese werden entsprechend des Tutorials von AndroidDevelopers genormt, um den DeltaRotationsVector und die DeltaRotationsMatrix zu
 * erhalten. Aus der Matrix werden anschließend die Winkel der aktuellen Geräteposition berechnet und auf ein Intervall von 0 bis 2 π
 * genormt. Addiert ergeben diese drei Winkel den Wert der aktuellen Rotations-intensität.
 */
public class GyroscopeService extends SensorDetectorServiceBase implements SensorEventListener{

    static final public String TAG = "GyroService";

    private SensorManager mSensorManager;
    private Sensor mGyroscope;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    /**
     *
     * @return Identifikator des Detektor
     */
    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Erzeugen des SensorManagers und Selbst-Anmeldung als Listener
     * Zusätzlich wird die ungefähre Abtast-Geschwindigkeit vorgegeben
     */
    @Override
    public final void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, mGyroscope, GlobalSettings.gGyroEventDelay );
    }

    /**
     * Vor dem Beenden den Listener wieder abmelden
     */
    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);

        super.onDestroy();
    }

    /**
     * Siehe Beispiel von AndroidDevelopers
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        checkActivity(GlobalSettings.gGyroEventWait, GlobalSettings.gGyroThreshold);

        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > 0.00001) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;

        double eulerX = Math.atan2(deltaRotationMatrix[7],deltaRotationMatrix[8]);
        double eulerY = Math.atan2(- deltaRotationMatrix[6], Math.sqrt(Math.pow(deltaRotationMatrix[7],2) + Math.pow(deltaRotationMatrix[8],2)));
        double eulerZ = Math.atan2(deltaRotationMatrix[3],deltaRotationMatrix[0]);

        double normEulerX = Math.abs(eulerX);
        double normEulerY = Math.abs(eulerY * 2);
        double normEulerZ = Math.abs(eulerZ);

        mDetectedSensorSum += normEulerX + normEulerY + normEulerZ;
    }

    /**
     * nicht relevant
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
