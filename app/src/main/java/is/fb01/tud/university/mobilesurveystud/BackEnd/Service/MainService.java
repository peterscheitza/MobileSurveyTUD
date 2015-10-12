package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Database.DatabaseConnector;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Receiver.DetectorReceiver;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.GPSDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService.PhoneDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService.SoundDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling.ServiceHandler;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling.ServiceStruct;
import is.fb01.tud.university.mobilesurveystud.FrontEnd.DialogActivity;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.FrontEnd.Notifier;
import is.fb01.tud.university.mobilesurveystud.R;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.TouchDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService.AccelerometerService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService.GyroscopeService;

/**
 * Created by peter_000 on 10.05.2015.
 */


public class MainService extends Service {

    static final String TAG = "MainService";

    public long mMillsStart = -1;
    public long mMillsEnd = -1;
    public boolean mIsScreenOn = true;
    public static ServiceHandler mServiceMap;

    private Context mContext;
    private SharedPreferences mSharedPref;
    private Handler mToastHandler;

    private DetectorReceiver mDetectorReceiver;
    private BroadcastReceiver mPowerButtonReceiver;
    private BroadcastReceiver mDialogReceiver;
    private BroadcastReceiver mWaitForUnlockReceiver;

    boolean mIsExtendedRunning = false;
    long mNextShowCounterUpdateDue = -1;

    /**
     * Als Erstes wird die foreground flag gesetzt. Diese sorgt dafür, dass der Service nicht aufgrund von
     * Speichermangel automatisch beendet wird
     *
     * Dann werden alle receiver initialisiert
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        Notification notification  = new Notifier(this).getForgroundNotification();
        startForeground(0,notification);

        mContext = this;

        mSharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(getString(R.string.setting_is_active), GlobalSettings.State.ON.toString());
        editor.putBoolean(getString(R.string.is_paused), false);
        editor.commit();

        mToastHandler = new Handler();

        initServiceMap();

        mDetectorReceiver = new DetectorReceiver(this);
        IntentFilter localFilter = new IntentFilter(DetectorServiceBase.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDetectorReceiver, localFilter);


        mDialogReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Lock Screen Activity");
                handleShowSurveyFromLockScreen();
            }
        };

        mWaitForUnlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from user present");
                handleUserPresent();
            }
        };

        /**
         * Receiver der informiert wird wenn der Bildschirmstatus (an/aus) wechselt
         */
        mPowerButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Screen: " + intent.toString() );
                handlePowerButton(intent);
            }
        };
        IntentFilter filterOnOff = new IntentFilter();
        filterOnOff.addAction(Intent.ACTION_SCREEN_OFF);
        filterOnOff.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mPowerButtonReceiver, filterOnOff);
    }

    /**
     * Wird beim Start des Service aufgerufen und gibt an, dass er im Fall eines automatischen
     * Beendens wieder startet
     * @param intent
     * @param flags
     * @param startId
     * @return der return value setzt die Flag zum neustarten
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Sicherheitshalber werden alle Receiver abgemeldet um sicherzugehen das keiner mehr aktiv sind
     *
     * Dann werden alle Detektoren abegeschaltet, indem alle Stopsituationen angenommen werden
     *
     * Abschließend wird geprüft ob settingIsActive auf OFF gesetzt wurde. In dem Fall darf der MainService
     * beendet werden. Sollte dies nicht der Fall sein wurde der Service außerplanmäßig beendet wurde. Dann
     * versucht sich der Service nach 30 Skeunden wieder zu starten.
     *
     * Beim Beenden des Service wird die Foregorund Flag entfernt
     */
    @Override
    public void onDestroy() {
        Log.v(TAG,"onServiceClose");

        try{ LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDetectorReceiver);}
        catch (IllegalArgumentException e) { Log.v(TAG, e.toString()); }
        try{LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDialogReceiver);}
        catch (IllegalArgumentException e) { Log.v(TAG, e.toString()); }
        try{unregisterReceiver(this.mWaitForUnlockReceiver);}
        catch (IllegalArgumentException e) { Log.v(TAG, e.toString()); }
        try{ unregisterReceiver(this.mPowerButtonReceiver);}
        catch (IllegalArgumentException e) { Log.v(TAG, e.toString()); }


        //stop all services
        for (ServiceStruct.stopSituation s : ServiceStruct.stopSituation.values())
            stopServiceEvent(s);

        String sIsMainState = mSharedPref.getString(getString(R.string.setting_is_active), GlobalSettings.State.UNDEFINED.toString());
        GlobalSettings.State isMainState = GlobalSettings.State.valueOf(sIsMainState);
        if(isMainState == GlobalSettings.State.ON) {
            Log.v(TAG, "not allowed to close, restart soon");

            goIdle(GlobalSettings.gTryToRestartMain);
        }
        else if(isMainState == GlobalSettings.State.OFF) {
                stopForeground(false);
            Log.v(TAG, "allowed to close,good bye");
        }
        else {
            Log.v(TAG, "could not get service state, close for safety reasons");
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Erstellen des ServiceHandler(Map) und füllen mit ServiceStructs und derenen Start- und Stopsituationen
     *
     * Abschließend wird angenommen das der Bildschirm an ist um initial zu testen, ob Aktivität vorliegt
     */
    private void initServiceMap(){

        mServiceMap = new ServiceHandler(this);


        List<ServiceStruct.stopSituation> offAndInactive = Arrays.asList(ServiceStruct.stopSituation.SCREEN_OFF, ServiceStruct.stopSituation.INACTIVITY);
        List<ServiceStruct.startSituation> phoneStart = Arrays.asList(ServiceStruct.startSituation.EXTEND, ServiceStruct.startSituation.SCREEN_OFF);

        mServiceMap.put(new TouchDetectionService(),  ServiceStruct.startSituation.SCREEN_ON, ServiceStruct.stopSituation.SCREEN_OFF);
        mServiceMap.put(new SoundDetectionService(),  ServiceStruct.startSituation.EXTEND, offAndInactive);
        mServiceMap.put(new PhoneDetectionService(), phoneStart, ServiceStruct.stopSituation.INACTIVITY);
        mServiceMap.put(new GyroscopeService(),  ServiceStruct.startSituation.EXTEND, offAndInactive);
        mServiceMap.put(new AccelerometerService(), ServiceStruct.startSituation.EXTEND, offAndInactive);
        mServiceMap.put(new GPSDetectionService(), ServiceStruct.startSituation.EXTEND, ServiceStruct.stopSituation.INACTIVITY);

        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            serviceStruct.isActive = false;
            serviceStruct.state = GlobalSettings.State.OFF;
        }

        startServiceEvent(ServiceStruct.startSituation.SCREEN_ON);
    }

    /**
     * Stoppt alle Services mit der übergebenen Stopsituation
     * @param stop
     */
    public void stopServiceEvent(ServiceStruct.stopSituation stop) {

        Log.v(TAG,"stopServiceEvent: " + stop );

        for (ServiceStruct serviceStruct : mServiceMap.values()) {
            if (serviceStruct.state == GlobalSettings.State.ON && serviceStruct.checkStop(stop)) {
                stopService(serviceStruct.mIntent);
                serviceStruct.state = GlobalSettings.State.OFF;
                serviceStruct.isActive = false;
            }
        }
    }

    /**
     * Startet alle Service mit der übergebenen Startsituation
     * @param start
     */
    public void startServiceEvent(ServiceStruct.startSituation start) {

        Log.v(TAG,"startServiceEvent: " + start );

        for (ServiceStruct serviceStruct : mServiceMap.values()) {
            if (serviceStruct.state == GlobalSettings.State.OFF && serviceStruct.checkStart(start)) {
                startService(serviceStruct.mIntent);
                serviceStruct.state = GlobalSettings.State.ON;
                serviceStruct.isActive = true; //assume activity
            }
        }
    }


    /**
     * Funktion für mPowerButtonReceiver
     * Startet und Stoppt alle Service, die von dem Bildschirmstatus abhängen
     * @param intent
     */
    private void handlePowerButton(Intent intent){
        Log.v(TAG,"screen turned off/on");

        if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
            mIsScreenOn = false;
            stopServiceEvent(ServiceStruct.stopSituation.SCREEN_OFF);
            startServiceEvent(ServiceStruct.startSituation.SCREEN_OFF);
        }
        else if(intent.getAction() == Intent.ACTION_SCREEN_ON) {
            mIsScreenOn = true;
            startServiceEvent(ServiceStruct.startSituation.SCREEN_ON);
        }
    }

    /**
     * Funktion für mDialogReceiver
     *
     * Wenn der Nutzer auf der auf dem Sperrbildschirm angezeigten Erinnerung den Fragebogen sehen möchte
     * Hierzu wird ein Intent gesendet und diese Funktion aufgerufen
     * Dann wird der mWaitForUnlockReceiver angemeldet der darauf wartet, das der Nutzer die
     * Sperrung des Gerätes aufhebt
     */
    private void handleShowSurveyFromLockScreen() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDialogReceiver);

        IntentFilter filterUserPresent = new IntentFilter();
        filterUserPresent.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mWaitForUnlockReceiver,filterUserPresent);
    }

    /**
     * Funktion für mWaitForUNlockreceiver
     *
     * Nachdem der Nutzer vom Sperrbildschirm aus die Umfrage sehen möchte
     * muss er sein Gerät entsperren. Nachdem er das getan hat wird in
     * Chrome (oder dem Standardbrowser) die Umfrage angezeigt
     *
     * Zusätzlich werden die unbenötigten Receiver abgemeldet und der MainService
     * (damit auch die Erfassung von Daten) für 30 min pausiert
     */
    private void handleUserPresent() {

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(GlobalSettings.gGetURLWithID(mContext)));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.android.chrome");
        try {
            startActivity(i);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            i.setPackage(null);
            startActivity(i);
        }

        unregisterReceiver(mWaitForUnlockReceiver);

        goIdle(GlobalSettings.gIdleAfterShow);
    }


    /**
     * Prüft ob, laut der ServiceStructs, Inaktivität vorliegt
     * @return
     */
    private boolean isInactivity(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == GlobalSettings.State.ON) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    /**
     * Zufallsfunktion, die entscheidet, ob der Dialog angezeigt wird
     *
     * Ermittelt eine Zufallszahl zwichen 0 und 100 und prüft
     * ob diese unter dem Schwellwert aus den GlobalSettings liegen
     * @return
     */
    private boolean randomFunction(){
        int randomInt = new Random().nextInt(100);
        return randomInt <= GlobalSettings.gPercentageToShow;
    }

    /**
     * Prüft ob der Dialog angezeigt werden muss und initziert das Anzeigen bei Beadrf
     *
     * Es wird in folgender Reihnefolge kontrolliert:
     * 1 Inaktivität
     * 2 Minimale Nutzungsdauer erreicht
     * 3 Zufallsfunktion
     * 4 Prüfen, ob der Dialog nicht zu oft angezeigt wurde
     * 5 Prüft, ob unerweünschte Prozesse im Hintergrund laufen
     * @return
     */
    public boolean isShowADialog() {
        if(isInactivity()) {

            long activityDuration = mMillsEnd - mMillsStart;
            Log.v(TAG, "!! INACTIVITY !! : activityDuration: " + activityDuration);

            if (activityDuration > GlobalSettings.gMinUseDuration) {
                if (isConnected()) {
                    if (randomFunction()) {
                        if (checkShownCounter()) {
                            if (!areAppsExceptional(getForegroundApps())) {
                                showADialog();

                                resetParameter();
                                return true;

                            } else
                                Log.v(TAG, "app is exceptional!");
                        } else
                            Log.v(TAG, "dialog was canceled due show counter");
                    } else
                        Log.v(TAG, "dialog was canceled due random function");
                } else
                    Log.v(TAG, "dialog was canceled - no internet connection");
            } else
            Log.v(TAG, "dialog was canceled due too short activity");

            resetParameter();
        }
        return false;
    }

    /**
     * Entscheidung, ob die Erinnerung als kleines oder großes Fenster angezeigt wird
     * Auf dem Sperrbildschirm muss die Erinnerung als Activity angezeigt werden
     */
    private void showADialog() {

        if (mIsScreenOn) {
            showSystemAlert();
        } else {
            showActivity();
        }
    }

    /**
     * Zurücksetzten aller wichtigen Parameter, wenn Inaktivität eingetreten ist
     */
    private void resetParameter(){
        mMillsStart = -1;
        mMillsEnd = -1;
        mIsExtendedRunning = false;
    }

    /**
     * Anzeigen der Erinnerung auf dem Sperrbildschirm als Activity
     * und registrieren des mDialogReceiver
     */
    private void showActivity() {
        Log.v(TAG, "show dialog activity");

        IntentFilter filterDialogActivity = new IntentFilter(DialogActivity.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDialogReceiver, filterDialogActivity);

        Intent i = new Intent(this, DialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    /**
     * Anzeigen der Umfragen Erinnerung als kleines Dialogfenster
     * Hierzu wird die DialogAgtivity herangezogen
     * Zusätzlich werden deie ButtoneListener angemeldet
     */
    private void showSystemAlert(){

        Log.v(TAG, "show dialog");

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_dialog);

        //dialog.setContentView(R.layout.dialog);
        dialog.setTitle(getString(R.string.dialog_head));

        TextView dialogText = (TextView) dialog.findViewById(R.id.activityText);
        dialogText.setText(getString(R.string.dialog_body));

        final Button dialogGoToButton = (Button) dialog.findViewById(R.id.activityGoToButton);
        dialogGoToButton.setText(getString(R.string.dialog_goto_button));

        final Button dialogExistButton = (Button) dialog.findViewById(R.id.activityExitButton);
        dialogExistButton.setText(getString(R.string.dialog_exit_button));

        WebView activityWebView = (WebView) dialog.findViewById(R.id.activityWebView);
        activityWebView.setVisibility(View.GONE);

        dialogGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialogGoToButton.setOnClickListener(null);
                dialogExistButton.setOnClickListener(null);

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gGetURLWithID(mContext)));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.android.chrome");
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException ex) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    i.setPackage(null);
                    startActivity(i);
                }

                goIdle(GlobalSettings.gIdleAfterShow);
            }
        });

        dialogExistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogGoToButton.setOnClickListener(null);
                dialogExistButton.setOnClickListener(null);
                dialog.dismiss();
            }
        });

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        new Notifier(this).alert();
    }

    /**
     * Anzeigen einer Systemnachricht (Toast)
     * @param sOutput
     */
    private void showToast(final String sOutput) {
        mToastHandler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(MainService.this, sOutput, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        Log.v(TAG,sOutput);
    }

    /**
     * Zurücksetzen des Zählers, der erhöht wird wenn die Erinnerung angezeigt wird     *
     *
     * @param lLastCounterUpdate    Zeitpunkt des letzen Updates
     * @param lCurrentTimeMills     Aktueller Zeitstempel
     */
    private void resetShowCounter(long lLastCounterUpdate, long lCurrentTimeMills) {
        Log.v(TAG,"reset show counter");

        long lNextUpdate = lLastCounterUpdate;

        do {
            lNextUpdate += GlobalSettings.gResetShowCounter;
            Log.v(TAG,"try to take " + lNextUpdate + " for next update");
        } while(lNextUpdate < lCurrentTimeMills);

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putLong(getString(R.string.nextCounterUpdate), lNextUpdate);
        editor.putInt(getString(R.string.dialogShownCounter), 0);
        editor.commit();

        mNextShowCounterUpdateDue = lNextUpdate;
    }

    /**
     * Prüft ob der Zähler der Angezeigten Dialogs zurück gesetzt werden muss
     * Dieses vorgehen ist robuster und energiesapramser als mit dem AlertManager
     * Der Zeitpunkt des letzten Updates wird auch als Member in diesem Service gespeichert
     * um nicht jedes mal auf die ShardPreference zuzugreifen (nur beim ersten mal)
     * @return
     */
    private boolean checkShownCounterUpdate(){
        long currentTimeMills = System.currentTimeMillis();

        if(mNextShowCounterUpdateDue < 0)
            mNextShowCounterUpdateDue = mSharedPref.getLong(getString(R.string.nextCounterUpdate), currentTimeMills);

        if(currentTimeMills >= mNextShowCounterUpdateDue) {
            Log.v(TAG,"reset show counter");

            resetShowCounter(mNextShowCounterUpdateDue, currentTimeMills);

            return true;
        }
        return false;
    }

    /**
     * Prüfen, ob die Erinnerungen nicht zu oft dem Nutzer angezeigt wurden
     *
     * Hiezu wird kontrolliert ob der Zähler der angezeigten Erinnerungen nicht
     * über dem Schwellwert in den GlobalSettings liegt.
     * Sollte dies nicht der Fall sein wird ein Anzeigen genehmigt und der Zähler
     * um eins erhöht
     *
     * @return  Bool der angbt, ob die Erinnerung angezeigt werden darf
     */
    private boolean checkShownCounter(){

        checkShownCounterUpdate();

        int lCounter = mSharedPref.getInt(getString(R.string.dialogShownCounter), 0);


        Log.v(TAG, "" + lCounter + " < " + GlobalSettings.gMaxShowCounter );
        if(lCounter < GlobalSettings.gMaxShowCounter){
            SharedPreferences.Editor editor = mSharedPref.edit();
            int newCounter = lCounter + 1;
            editor.putInt(getString(R.string.dialogShownCounter), newCounter);
            editor.commit();
            return true;
        }
        else {

            long lLastCounterUpdate = mSharedPref.getLong(getString(R.string.nextCounterUpdate), 0);
            if(lLastCounterUpdate == 0) {
                Log.v(TAG, "unable to read last counter update to set alarm manager");
                assert true;
                return false;
            }

            long lSleepDuration = (lLastCounterUpdate + GlobalSettings.gResetShowCounter) - System.currentTimeMillis();
            goIdle(lSleepDuration);

            Log.v(TAG, "shown dialog to much, wait for: " + lSleepDuration);

            stopSelf();
            return false;
        }
    }

    /**
     * Vergeleichen einer Listen von Prozessen mit den unerwünschten Apps aus den GlobalSettings
     * Diese könnte auch aus dem DatabaseConnector ausgelesen werden, dies wurde aber verworfen
     *
     * @param appeNameVec
     * @return
     */
    private boolean areAppsExceptional( Vector<String> appeNameVec){
        //DatabaseConnector connector = new DatabaseConnector(this);
        //Vector<String> exceptionalAppsVec = connector.readAllEntrys();

        Vector<String> exceptionalAppsVec = new Vector<>();
        exceptionalAppsVec.addAll(Arrays.asList(GlobalSettings.gDefaultExceptionalApps));

        for(String currentExcept : exceptionalAppsVec){
            for(String currentCheck : appeNameVec) {
                if (currentCheck.equals(currentExcept)) {
                    Log.v(TAG, "is exceptional app: " + currentExcept);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Erstellt eine Liste aller Processe die mit hoher Priorität laufen
     *
     * @return  List an Prozessen
     */
    private Vector<String> getForegroundApps() {

        Vector<String> toReturn = new Vector<>();

        ActivityManager mActivityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lRunningProc = mActivityManager.getRunningAppProcesses();

        if(lRunningProc == null)
            return toReturn;


        for(ActivityManager.RunningAppProcessInfo info : lRunningProc) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND )
                toReturn.add(info.processName);
        }
        return toReturn;
    }

    /**
     * Prüft ob eine Internetverbindung besteht um den Fragebogen anzuzeigen
     *
     * @return boolean
     */
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Pausiert die Anwendung mittels eiens AlaramManager
     * Hierbei wird auch der MainService beendet
     *
     * @param lIdleTime Dauer der Pausierung
     */
    private void goIdle(long lIdleTime){
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(getString(R.string.setting_is_active), GlobalSettings.State.OFF.toString());
        editor.putBoolean(getString(R.string.is_paused), true);
        editor.commit();

        Intent intent = new Intent(this, MainService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        try {
            alarm.cancel(pintent); //clear for safety reasons
        } catch (Exception e) {
            Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
        }

        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lIdleTime, pintent);

        stopSelf();

        Log.v(TAG, "going idle for: " + lIdleTime);
    }

    private void addAppToExceptionList(String sAppName) {
        DatabaseConnector connector = new DatabaseConnector(this);

        connector.insert(sAppName);
    }
}
