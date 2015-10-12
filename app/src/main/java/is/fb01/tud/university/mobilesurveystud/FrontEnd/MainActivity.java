package is.fb01.tud.university.mobilesurveystud.FrontEnd;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Bei der MainActivity handelt es sich um den initialen Eintrittspunkt in die Anwendung (siehe das Kapitel „AndoridManifest“),
 * die alle für den Umfrageteilnehmer relevanten Einstellungen bereitstellt.
 */
public class MainActivity extends Activity {

    public static final String PACKAGE_NAME = "is.fb01.tud.university.mobilesurveystud";

    static final String TAG = "MainActivity";

    private Context mContext;

    private SharedPreferences mSharedPref;
    private Intent mMainService;

    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmIntent;

    /**
     * Zunächst werden hier die SharedPreferences gelesen und gegebenfalls der MainService gestartet
     *
     * Als nächstes wird der AlaramManager und alle benötigten Daten Inizialisert um den Service zu pausieren
     *
     * Abschließend wird der Numberpicker (Auswahl an Stunden, die die ANwendung pasuiert werden soll) inizialisiert
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //READ DATA AND START SERVICE IF NEEDED
        mSharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        GlobalSettings.State useMainService = readEnum(R.string.setting_is_active);

        //set default value on first init
        if(useMainService == GlobalSettings.State.UNDEFINED) {
            SharedPreferences.Editor editor = mSharedPref.edit();

            editor.putString(getString(R.string.setting_is_active), GlobalSettings.gDefaultMainService.toString());
            useMainService = GlobalSettings.gDefaultMainService;

            editor.commit();
        }


        mMainService = new Intent(this, MainService.class);

        if(!isServiceRunning(MainService.class) && useMainService == GlobalSettings.State.ON )
            startService(mMainService);

        //ALARM
        Intent intent = new Intent(this, MainService.class);
        mAlarmIntent = PendingIntent.getService(this, 0, intent, 0);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        //NUMBERPICKER
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker1);
        int arrayRange = GlobalSettings.gMaxIdleHours - GlobalSettings.gMinIdleHours + 1;
        String[] nums = new String[arrayRange];
        for(int i=0; i<nums.length; i++)
            nums[i] = (Integer.toString(GlobalSettings.gMinIdleHours+i) + " " + getString(R.string.settings_hours)) ;

        np.setMinValue(GlobalSettings.gMinIdleHours);
        np.setMaxValue(GlobalSettings.gMaxIdleHours);
        np.setWrapSelectorWheel(true);
        np.setDisplayedValues(nums);
        np.setValue(GlobalSettings.gMinIdleHours);
    }

    /**
     * Sollte die ANwendung wieder Fortgesetzt werden kann sich der MainService beendent haben und somit muss das
     * ForntEnd geupdatet werden
     */
    @Override
    public void onResume(){
        super.onResume();

        updateFrontEndText();
    }

    /**
     * Diese Funktion wird aufgerufen, wenn der Nutzer den MainService an- oder abschalten will
     *
     * Sollte der MainService deaktiviert werden wird zuerst ein Dialog angezeigt (siehe Funktion showStopDialog())
     *
     * @param v Schaltfläche die gedrückt worden ist
     */
    public void buttonToggleService(View v){

        if(isServiceRunning(MainService.class)){
            showStopDialog();
        }
        else{
            startService(mMainService);
            Toast.makeText(this, getString(R.string.settings_toast_start), Toast.LENGTH_SHORT).show();

            //cancel any restart demands - paranoia
            try {
                mAlarmManager.cancel(mAlarmIntent); //clear for safety reasons
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
            }

            updateFrontEndText();

            //happens now in MainService
            //editor.putString(getString(R.string.setting_is_active), MainService.State.ON.toString());
        }
    }

    /**
     * Diese Funktion wird aufgerufen, wenn der Nutzer die Identifikation speichern will
     *
     * @param v Schaltfläche die gedrückt worden ist
     */
    public void buttonSaveId(View v){

        SharedPreferences.Editor editor = mSharedPref.edit();

        EditText edittext = (EditText) findViewById(R.id.editText);
        String sId = edittext.getText().toString().trim();

        //only save non-empty strings
        if(!sId.equals("") && sId != null && !sId.isEmpty()) {
            editor.putString(getString(R.string.user_id), sId);
            editor.commit();

            Toast.makeText(this, getString(R.string.settings_toast_save) + " " + sId , Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Diese Funktion wird aufgerufen, wenn der Nutzer die Erfssung (und damit auch die Erinnerungen) pasuieren möchte.
     * Sollte der Service bereits inaktiv sein wird der Nutzer darauf hingewiesen
     *
     * Es wird hier mit einem Alaram gearbeitet, der entsprechend der im Numberpicker eingestellten Stunden, den MainService
     * wieder staret
     *
     * @param v Schaltfläche die gedrückt worden ist
     */
    public void buttonGoIdle(View v) {

        if(isServiceRunning(MainService.class)) {

            NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker1);
            long lChoosenNumb = np.getValue();
            long lIdleTime = lChoosenNumb * 1000 * 60 * 60;

            stopMainService(true);


            try {
                mAlarmManager.cancel(mAlarmIntent); //clear for safety reasons
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
            }

            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lIdleTime, mAlarmIntent);

            Log.v(TAG, "going idle on user demand for: " + lIdleTime);

            Toast.makeText(this, "" + getString(R.string.settings_toast_paused) + " " + lChoosenNumb + " " + getString(R.string.settings_hours), Toast.LENGTH_SHORT).show();

            updateFrontEndText();
        }
        else {
            Toast.makeText(this, getString(R.string.settings_toast_is_paused), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Update der Buttonbeschriftungen und Textboxen
     */
    private void updateFrontEndText(){
        Button toggleMainButton = (Button) findViewById(R.id.mainToggleService);

        if(isServiceRunning(MainService.class))
            toggleMainButton.setText(getString(R.string.settings_stop_button));
        else
            toggleMainButton.setText(getString(R.string.settings_start_button));

        String sId = mSharedPref.getString(getString(R.string.user_id), "");
        EditText edittext = (EditText) findViewById(R.id.editText);
        edittext.setText(sId);
    }

    /**
     * Der Nutzer bekommt einen Dialog mit einer "Annehmen"- und einer "Ablehen"-Schaltfläche angezeigt
     *
     * Beim Ablehenen passiert nichts
     *
     * Beim Annehemn wird der MainServiceBeendet und nach einer Sekunde das FrontEnd upgedatet
     */
    private void showStopDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(getString(R.string.settings_stop_dialog_head));
        dialogBuilder.setMessage(getString(R.string.settings_stop_dialog_body));

        dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, getString(R.string.settings_toast_stop), Toast.LENGTH_SHORT).show();

                        stopMainService(false);

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                updateFrontEndText();
                            }};

                        Handler mHandler = new Handler();
                        mHandler.postDelayed(r, 2000); //wait for slow devices until main is stopped - paranoia and looks better
                    }
                });

        dialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.show();
    }

    /**
     * Diese Funktion stoppt den MainService auf die korrekte weise in dem die SharedPreference setting_is_active auf OFF
     * gesetzt wird. Dadruch wird von dem MainService keine Alarm zum Neustarten gesetzt
     * @param bIsPause
     */
    private void stopMainService(boolean bIsPause) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(getString(R.string.setting_is_active), GlobalSettings.State.OFF.toString());
        editor.putBoolean(getString(R.string.is_paused), bIsPause);
        editor.commit();

        stopService(mMainService);
    }


    //---------HELPER----------

    /**
     * Prüft ob der Service aktiv ist
     * @param serviceClass zu prüfender Service
     * @return
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ließt eine  Enum aus den SharedPreferences
     * @param iName
     * @return
     */
    private GlobalSettings.State readEnum(int iName){
        String optioneName = getString(iName);
        String sEnum = mSharedPref.getString(optioneName, GlobalSettings.State.UNDEFINED.toString());
        return GlobalSettings.State.valueOf(sEnum);
    }
}
