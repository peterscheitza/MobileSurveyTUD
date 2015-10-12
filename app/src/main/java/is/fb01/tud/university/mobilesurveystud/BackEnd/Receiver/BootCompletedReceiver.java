package is.fb01.tud.university.mobilesurveystud.BackEnd.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.R;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;

/**
 * Created by peter_000 on 25.05.2015.
 * Receiver um den Start des Systems zu erfassen
 *
 * Die Klasse BootCompleteReceiver erweitert einen BroadcastReceiver und wird im Manifest für das Intent BOOT_COMPLETE angemeldet.
 * Diese Benachrichtigung wird automatisch von Android gesendet, wenn der Startvorgang des Gerätes abgeschlossen ist. Er kann nur
 * von Applikationen empfangen werden, die die Berechtigung RECEIVE_BOOT_ COMPLETED haben (siehe Kapitel „Permissions“).
 *
 * Sollte der Nutzer sein Gerät neustarten, wird Mobile Survey TUD immer durch diesen Receiver informiert. Hierbei wird
 * die SharedPreference SettingIsActive gelesen und überprüft, ob sie auf ON gesetzt wurde. Das bedeutet, dass die Anwendung während
 * des Herunterfahrens von Android aktiv war und jetzt wieder gestartet werden muss. Zusätzlich wird kontrolliert, ob die App
 * vom Nutzer pausiert wurde. Hierzu dient die Preference isPaused, die beim Pausieren der Anwendung für einige Stunden gesetzt wird.
 * Diese zusätzliche Preference gewährleistet nicht, dass die Anwendung wieder in einem pausierten Zustand übergeht, wenn das Gerät
 * neugestartet wird. Sie stellt lediglich sicher, dass die Anwendung beim Einschalten des Geräts wieder korrekt startet.
 * Es wäre möglich die Erfassung beim Systemstart wieder zu pausieren, was vom Nutzer nicht favorisiert wird.
 */
public class BootCompletedReceiver extends BroadcastReceiver{

    static final public String TAG = "BootCompletedReceiver";

    /**
     * Der Bootvorgang ist abegsclossen
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) { //just to be sure
            handleBootCompleteReceive(context);
        }
    }

    /**
     * Es wird geprüft, ob beim letzten Herunterfahren Die Erfassung aktiv oder pausiert war
     * In diesem Fall wird die Erfassung wieder gestartet.
     * @param context
     */
    private void handleBootCompleteReceive(Context context){
        Resources r = context.getResources();

        //Read SharedPreferences
        String sharedPrefName = r.getString(R.string.shared_Pref);
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        String optioneStateName = r.getString(R.string.setting_is_active);
        String lastSavedState = sharedPref.getString(optioneStateName, GlobalSettings.State.UNDEFINED.toString());

        String optionePausedName = r.getString(R.string.is_paused);
        boolean isPaused = sharedPref.getBoolean(optionePausedName, false);

        Log.v(TAG, sharedPrefName);
        Log.v(TAG,optioneStateName);
        Log.v(TAG,"lastSavedState: " + lastSavedState);
        Log.v(TAG,"isPaused: " + isPaused);


        //Start if Service was on or paused
        if(lastSavedState.equals(GlobalSettings.State.ON.toString()) || isPaused) {
            Log.v(TAG, "restart service");
            Intent iMainService = new Intent(context, MainService.class);
            context.startService(iMainService);
        }
        else if(lastSavedState.equals(GlobalSettings.State.OFF.toString())) {
            //do nothing
            Log.v(TAG, "service was off, we keep it off");
        }
        else{
            Log.v(TAG, "state undefined" + lastSavedState);
        }
    }
}
