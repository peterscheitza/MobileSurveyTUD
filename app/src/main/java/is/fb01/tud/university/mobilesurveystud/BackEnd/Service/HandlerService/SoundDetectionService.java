package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService;

import android.content.Context;
import android.media.AudioManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.EventDetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 05.06.2015.
 * Prüft ob gerade Ton abgespielt wird. Dieser Detektor wurde nötig um
 * festzustellen ob der Nutzer ein Video schaut, der er dabei das Display nicht berührt.
 * Dies kann auch genutzt werden um Musik hören als Aktivität zu werten
 *
 * Ähnlich wie der PhoneDetecionService prüft der SoundDetectionService mittels des Systemdienstes AudioManager  die Tonausgabe des
 * Gerätes. In einem Intervall von zehn Sekunden wird kontrolliert, ob Musik abgespielt wird. Hierdurch lässt sich die Annahme von
 * Inaktivität beim Abspielen von Videos über die Einstellungen der ServiceStructs verhindern. Das Problem der Videowiedergabe ist,
 * dass der Nutzer das Gerät über einen längeren Zeitraum stillhält und nicht den Bildschirm berührt. Dies kann von keinem anderen
 * Detektor erkannt werden.
 * Dieser Service eignet sich auch für eine anderweitige Verwendung. Dazu wird in den ServiceStructs eingestellt, dass der Service
 * beim Abstellen des Bildschirmes nicht deaktiviert werden soll und dadurch Inaktivität angenommen wird. Als Resultat wird auch das
 * Hören von Musik als Nutzung gewertet, sodass dem Studienteilnehmer noch kein Dialogfeld angezeigt wird.
 */
public class SoundDetectionService extends HandlerDetectorServiceBase{

    static final public String TAG = "Sound Service";

    AudioManager mAudioManager;

    /**
     *
     * @return Identifikator des Detektor
     */
    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Inizialiserung des AudioManagers
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Festlegung des Intervalles indem geprüft werden soll
     * @return
     */
    @Override
    long getHandlerDelay() {
        return GlobalSettings.gSoundEventWait;
    }

    /**
     * Prüft ob Musik abgespielt wird
     * @return
     */
    @Override
    boolean conditionToCheck() {
        return mAudioManager.isMusicActive();
    }

    /**
     * nicht zu beachten
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
