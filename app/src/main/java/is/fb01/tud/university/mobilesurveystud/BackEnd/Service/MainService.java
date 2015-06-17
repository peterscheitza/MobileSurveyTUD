package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import is.fb01.tud.university.mobilesurveystud.BackEnd.DatabaseConnector;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.GPSDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService.PhoneDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService.SoundDetectionService;
import is.fb01.tud.university.mobilesurveystud.FrontEnd.DialogActivity;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.Notifier;
import is.fb01.tud.university.mobilesurveystud.R;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.TouchDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService.AccelerometerService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService.GyroscopeService;

/**
 * Created by peter_000 on 10.05.2015.
 */


public class MainService extends Service {

    static final String TAG = "MainService";

    static public enum State {
        ON,OFF,UNDEFINED
    }

    private SharedPreferences mSharedPref;

    private Handler mToastHandler;

    private BroadcastReceiver mLocalBroadcasteReceiver;

    private BroadcastReceiver mPowerButtonReceiver;
    private BroadcastReceiver mDialogReceiver;
    private BroadcastReceiver waitForUnlockReceiver;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;
    private boolean mIsScreenOn = true;

    HashMap<String,ServiceStruct> mServiceMap = new HashMap<>();

    boolean mIsExtendedRunning = false;

    State mIsGyroState;
    State mIsUseAdditional;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        Notification notification  = new Notifier(this).getForgroundNotification();
        startForeground(1,notification);


        mSharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);

        String optionGyro = getString(R.string.setting_is_gyro);
        String sGyroState = sharedPref.getString(optionGyro, State.UNDEFINED.toString());
        mIsGyroState = State.valueOf(sGyroState);

        String optionAdditional = getString(R.string.setting_is_additional);
        String sAddState = sharedPref.getString(optionAdditional, State.UNDEFINED.toString());
        mIsUseAdditional = State.valueOf(sAddState);

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(getString(R.string.setting_is_active), MainService.State.ON.toString());
        editor.commit();

        mToastHandler = new Handler();


        initServiceMaps();


        //Standard Receiver registration
        mLocalBroadcasteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String sender = intent.getStringExtra(getString(R.string.sender));
                String serviceType = intent.getStringExtra(getString(R.string.serviceType));
                boolean isActive = intent.getBooleanExtra(getString(R.string.is_active), false);
                long millsStart = intent.getLongExtra(getString(R.string.millsStart), -1);
                long millsEnd = intent.getLongExtra(getString(R.string.millsEnd) , -1);

                Log.v(TAG,"onReceiveMessage from Service: " + sender + " isActive: " + isActive );

                assert millsStart != -1;
                assert millsEnd   != -1;

                if(mMillsStart == -1)
                    mMillsStart = millsStart;

                if(mMillsEnd < millsEnd)
                    mMillsEnd = millsEnd;

                if(mServiceMap.containsKey(sender)){
                    ServiceStruct struct = mServiceMap.get(sender);

                    struct.isActive = isActive;
                    if(struct.checkStop(ServiceStruct.stopSituation.INACTIVITY) && !isActive) {
                        stopService(struct.mIntent);
                        struct.state = State.OFF;
                    }
                }
                else {
                    Log.v(TAG, "something went wrong. service not found");
                    assert false;
                }


                if (!mIsExtendedRunning && isStandardInactivity() && mIsScreenOn) {
                    startServiceEvent(ServiceStruct.startSituation.EXTEND);
                    mIsExtendedRunning = true;
                }


                isShowADialog();
            }
        };
        IntentFilter localFilter = new IntentFilter(DetectorServiceBase.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcasteReceiver, localFilter);


        mDialogReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Lock Screen Activity");
                handleShowSurveyFromLockScreen(intent);
            }
        };
        IntentFilter filterDialogActivity = new IntentFilter(DialogActivity.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDialogReceiver, filterDialogActivity);


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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.v(TAG,"onServiceClose");

        //LocalBroadcastManager.getInstance(this)....
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mLocalBroadcasteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDialogReceiver);
        unregisterReceiver(this.mPowerButtonReceiver);

        //stop all services
        for (ServiceStruct.stopSituation s : ServiceStruct.stopSituation.values())
            stopServiceEvent(s);

        String sIsMainState = mSharedPref.getString(getString(R.string.setting_is_active),State.UNDEFINED.toString());
        State isMainState = State.valueOf(sIsMainState);
        if(isMainState == State.ON) {
            Log.v(TAG, "not allowed to close, restart soon");
            Intent intent = new Intent(this, MainService.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30 * 1000, pintent);
        }
        else if(isMainState == State.OFF) {
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





    private void initServiceMaps(){

        Intent touchDetectionService = new Intent(this, TouchDetectionService.class);
        //Intent buttonDetectionService = new Intent(this, ButtonDetectionService.class);

        Intent soundDetectionService = new Intent(this, SoundDetectionService.class);
        Intent gpsDetectionService = new Intent(this, GPSDetectionService.class);
        Intent gyroService = new Intent(this, GyroscopeService.class);
        Intent acceleromterService = new Intent(this, AccelerometerService.class);
        Intent phoneService = new Intent(this, PhoneDetectionService.class);

        mServiceMap.put(TouchDetectionService.TAG, new ServiceStruct(touchDetectionService, ServiceStruct.startSituation.SCREEN_ON, ServiceStruct.stopSituation.SCREEN_OFF));

        List<ServiceStruct.stopSituation> soundStop = Arrays.asList(ServiceStruct.stopSituation.SCREEN_OFF, ServiceStruct.stopSituation.INACTIVITY);
        mServiceMap.put(SoundDetectionService.TAG, new ServiceStruct(soundDetectionService, ServiceStruct.startSituation.EXTEND, soundStop));

        List<ServiceStruct.startSituation> phoneStart = Arrays.asList(ServiceStruct.startSituation.EXTEND, ServiceStruct.startSituation.SCREEN_OFF);
        mServiceMap.put(PhoneDetectionService.TAG, new ServiceStruct(phoneService, phoneStart, ServiceStruct.stopSituation.INACTIVITY));

        if(mIsUseAdditional == State.ON) {
            mServiceMap.put(GyroscopeService.TAG, new ServiceStruct(gyroService, ServiceStruct.startSituation.EXTEND, ServiceStruct.stopSituation.INACTIVITY));
            mServiceMap.put(AccelerometerService.TAG, new ServiceStruct(acceleromterService, ServiceStruct.startSituation.EXTEND, ServiceStruct.stopSituation.INACTIVITY));
        }

        if(mIsGyroState == State.ON)
            mServiceMap.put(GPSDetectionService.TAG, new ServiceStruct(gpsDetectionService, ServiceStruct.startSituation.EXTEND, ServiceStruct.stopSituation.INACTIVITY));

        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            serviceStruct.isActive = false;
            serviceStruct.state = State.OFF;
        }

        startServiceEvent(ServiceStruct.startSituation.SCREEN_ON);
    }

    private void stopServiceEvent(ServiceStruct.stopSituation stop) {

        Log.v(TAG,"stopServiceEvent: " + stop );

        for (ServiceStruct serviceStruct : mServiceMap.values()) {
            if (serviceStruct.state == State.ON && serviceStruct.checkStop(stop)) {
                stopService(serviceStruct.mIntent);
                serviceStruct.state = State.OFF;
                serviceStruct.isActive = false;
            }
        }
    }

    private void startServiceEvent(ServiceStruct.startSituation start) {

        Log.v(TAG,"startServiceEvent: " + start );

        for (ServiceStruct serviceStruct : mServiceMap.values()) {
            if (serviceStruct.state == State.OFF && serviceStruct.checkStart(start)) {
                startService(serviceStruct.mIntent);
                serviceStruct.state = State.ON;
                serviceStruct.isActive = true; //assume activity
            }
        }
    }






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

    private void handleShowSurveyFromLockScreen(Intent intent) {
         waitForUnlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from user present");

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gGetURLWithID()));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                unregisterReceiver(waitForUnlockReceiver);
            }
        };

        IntentFilter filterUserPresent = new IntentFilter();
        filterUserPresent.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(waitForUnlockReceiver,filterUserPresent);
    }






    private boolean isInactivity(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == State.ON) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    private boolean isStandardInactivity(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == State.ON && !serviceStruct.checkStart(ServiceStruct.startSituation.EXTEND)) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }







    private boolean randomFunction(){
        int randomInt = new Random().nextInt(100);
        return randomInt <= GlobalSettings.gPercentageToShow;
    }

    private boolean isShowADialog() {
        long activityDuration = mMillsEnd - mMillsStart;

        Log.v(TAG, "activityDuration: " + activityDuration);

        if(isInactivity()) {
            if (activityDuration > GlobalSettings.gMinUseDuration) {
                if(randomFunction()) {
                    showADialog();

                    resetParameter();
                    return true;
                }
                Log.v(TAG, "dialog was canceled due random function");
            }
            Log.v(TAG, "dialog was canceled due too short activity");
            resetParameter();
        }
        return false;
    }

    private void showADialog(){
        if(checkShownCounter()) {
            showToast("Please answer!");
            if (mIsScreenOn) {
                if (!areAppsExceptional(getForegroundApps()))
                    showSystemAlert();
                else
                    Log.v(TAG, "app is exceptional!");
            } else {
                showActivity();
            }
        }
    }

    private void resetParameter(){
        mMillsStart = -1;
        mMillsEnd = -1;
        mIsExtendedRunning = false;
    }

    private void showActivity() {
        Log.v(TAG, "show dialog activity");

        Intent i = new Intent(this, DialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void showSystemAlert(){

        Log.v(TAG, "show dialog");

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_dialog);

        //dialog.setContentView(R.layout.dialog);
        dialog.setTitle(GlobalSettings.gDialogHead);

        TextView dialogText = (TextView) dialog.findViewById(R.id.activityText);
        dialogText.setText(GlobalSettings.gDialogBody);

        Button dialogGoToButton = (Button) dialog.findViewById(R.id.activityGoToButton);
        dialogGoToButton.setText(GlobalSettings.gDialogGoToButton);

        Button dialogExistButton = (Button) dialog.findViewById(R.id.activityExitButton);
        dialogExistButton.setText(GlobalSettings.gDialogExistButton);

        WebView activityWebView = (WebView) dialog.findViewById(R.id.activityWebView);
        activityWebView.setVisibility(View.GONE);

        dialogGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gGetURLWithID()));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        dialogExistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        new Notifier(this).alert();
    }

    private void showToast(final String sOutput) {
        mToastHandler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(MainService.this, sOutput, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        Log.v(TAG,sOutput);
    }




    //besser als alert
    private boolean checkShownCounterUpdate(){
        long lLastCounterUpdate = mSharedPref.getLong(getString(R.string.lastCounterUpdate), 0);
        long nextUpdateDue = lLastCounterUpdate + GlobalSettings.gResetShowCounter;
        long currentTimeMills = System.currentTimeMillis();

        if(currentTimeMills > nextUpdateDue) {
            Log.v(TAG,"reset show counter");

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putLong(getString(R.string.lastCounterUpdate), currentTimeMills);
            editor.putInt(getString(R.string.dialogShownCounter), 0);
            editor.commit();
            return true;
        }
        return false;
    }

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

            long lLastCounterUpdate = mSharedPref.getLong(getString(R.string.lastCounterUpdate), 0);
            if(lLastCounterUpdate == 0) {
                Log.v(TAG, "unable to read last counter update to set alarm manager");
                assert true;
                return false;
            }

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(getString(R.string.setting_is_active), MainService.State.OFF.toString());
            editor.commit();

            long lSleepDuration = (lLastCounterUpdate + GlobalSettings.gResetShowCounter) - System.currentTimeMillis();

            Intent intent = new Intent(this, MainService.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), lSleepDuration, pintent);

            stopSelf();

            Log.v(TAG, "shown dialog to much, wait for: " + lSleepDuration);
            return false;
        }
    }

    private boolean areAppsExceptional( Vector<String> appeNameVec){
        DatabaseConnector connector = new DatabaseConnector(this);

        Vector<String> exceptionalAppsVec = connector.readAllEntrys();

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

    private void addAppToExceptionList(String sAppName) {
        DatabaseConnector connector = new DatabaseConnector(this);

        connector.insert(sAppName);
    }

    private boolean isGPSEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return  (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
}
