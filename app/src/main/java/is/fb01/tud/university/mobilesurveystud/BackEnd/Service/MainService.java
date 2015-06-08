package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.ButtonDetectionService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.SoundDetectionService;
import is.fb01.tud.university.mobilesurveystud.FrontEnd.DialogActivity;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
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

    static public enum ServiceType {
        STANDARD,ADDITIONAL
    }

    private BroadcastReceiver mLocalBroadcasteReceiver;

    private BroadcastReceiver mPowerButtonReceiver;
    private BroadcastReceiver mDialogReceiver;
    private BroadcastReceiver waitForUnlockReceiver;


    private Handler mToastHandler;


    private long mMillsStart = -1;
    private long mMillsEnd = -1;
    private boolean mIsScreenOn = true;

    HashMap<String,ServiceStruct> mServiceMap = new HashMap<>();

    boolean mIsUseAdditional = true;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        mToastHandler = new Handler();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        String optioneName = getString(R.string.is_gyro);
        String sGyroState = sharedPref.getString(optioneName, State.UNDEFINED.toString());



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

                Log.v(TAG,"onReceiveMessage from Service: " + sender);

                assert millsStart != -1;
                assert millsEnd   != -1;

                if(mMillsStart == -1)
                    mMillsStart = millsStart;

                if(mMillsEnd < millsEnd)
                    mMillsEnd = millsEnd;

                if(mServiceMap.containsKey(sender)){
                    ServiceStruct struct = mServiceMap.get(sender);

                    struct.isActive = isActive;
                    if(struct.type == ServiceType.ADDITIONAL && !isActive)
                        stopService(struct.intent);
                }
                else {
                    Log.v(TAG, "something went wrong. service not found");
                    assert false;
                }

                if(mIsUseAdditional) {
                    if (isStandardInactivity() && mIsScreenOn)
                        startAdditionalServices();
                    else
                        stopAdditionalServices();
                }


                isShowADialog();
            }
        };
        IntentFilter localFilter = new IntentFilter(DetectorServiceBase.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcasteReceiver, localFilter);



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



        mDialogReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Lock Screen Activity");
                handleShowSurveyFromLockScreen(intent);
            }
        };
        IntentFilter filterDialogActivity = new IntentFilter(DialogActivity.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDialogReceiver, filterDialogActivity);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //LocalBroadcastManager.getInstance(this)....
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mLocalBroadcasteReceiver);
        unregisterReceiver(this.mPowerButtonReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDialogReceiver);

        stopAdditionalServices();
        stopStandardServices();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    private void initServiceMaps(){

        Intent touchDetectionService = new Intent(this, TouchDetectionService.class);
        //Intent buttonDetectionService = new Intent(this, ButtonDetectionService.class);

        Intent soundDetectionService = new Intent(this, SoundDetectionService.class);
        Intent gyroService = new Intent(this, GyroscopeService.class);
        Intent acceleromterService = new Intent(this, AccelerometerService.class);

        mServiceMap.put(TouchDetectionService.TAG, new ServiceStruct(touchDetectionService, ServiceType.STANDARD));

        mServiceMap.put(SoundDetectionService.TAG, new ServiceStruct(soundDetectionService, ServiceType.ADDITIONAL));
        mServiceMap.put(GyroscopeService.TAG, new ServiceStruct(gyroService, ServiceType.ADDITIONAL));
        mServiceMap.put(AccelerometerService.TAG, new ServiceStruct(acceleromterService, ServiceType.ADDITIONAL));

        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.type == ServiceType.STANDARD) {
                startService(serviceStruct.intent);
                serviceStruct.state = State.ON;
            }
            else if (serviceStruct.type == ServiceType.ADDITIONAL) {
                serviceStruct.state = State.OFF;

            }
            else
                Log.v(TAG, "wrong service type"); assert false;
        }
    }

    private void startAdditionalServices() {
        for (ServiceStruct serviceStruct : mServiceMap.values()) {
            if (serviceStruct.state == State.OFF && serviceStruct.type == ServiceType.ADDITIONAL) {
                startService(serviceStruct.intent);
                serviceStruct.state = State.ON;
                serviceStruct.isActive = true; //assume activity first
            }
        }
    }

    private void stopAdditionalServices(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == State.ON && serviceStruct.type == ServiceType.ADDITIONAL) {
                stopService(serviceStruct.intent);
                serviceStruct.state = State.OFF;
                serviceStruct.isActive = false;
            }
        }
    }

    private void startStandardServices(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == State.OFF && serviceStruct.type == ServiceType.STANDARD) {
                startService(serviceStruct.intent);
                serviceStruct.state = State.ON;
                serviceStruct.isActive = false;
            }
        }
    }

    private void stopStandardServices(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == State.ON && serviceStruct.type == ServiceType.STANDARD) {
                stopService(serviceStruct.intent);
                serviceStruct.state = State.OFF;
                serviceStruct.isActive = false;
            }
        }
    }

    private void handlePowerButton(Intent intent){
        Log.v(TAG,"screen turned off/on");

        if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
            mIsScreenOn = false;
            stopStandardServices();
            stopAdditionalServices();
        }
        else if(intent.getAction() == Intent.ACTION_SCREEN_ON) {
            mIsScreenOn = true;
            //resetParameter();
            startStandardServices();
        }
    }

    private void handleShowSurveyFromLockScreen(Intent intent) {
         waitForUnlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from user present");

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gSurveyURL));
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
            if(serviceStruct.state == State.ON && serviceStruct.type == ServiceType.STANDARD) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    private boolean isAdditionalInactivity(){
        for ( ServiceStruct serviceStruct : mServiceMap.values()) {
            if(serviceStruct.state == State.ON && serviceStruct.type == ServiceType.ADDITIONAL) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    private boolean isShowADialog() {
        long activityDuration = mMillsEnd - mMillsStart;

        Log.v(TAG, "activityDuration: " + activityDuration);

        if(isInactivity()) {
            if (activityDuration > GlobalSettings.gMinUseDuration) {
                showToast("MS: Please answer survey, bitch!");

                showADialog();

                resetParameter();
                return true;
            }

            resetParameter();
        }
        return false;
    }

    private void showADialog(){
        if(checkShownCounter()) {
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
    }

    private void showNotification(){
        Log.v(TAG, "notification");

        Notification notification  = new Notification.Builder(this).build();
        notification.defaults =  Notification.DEFAULT_ALL;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private void showActivity() {
        Log.v(TAG, "show dialog activity");

        Intent i = new Intent(this, DialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                showNotification(); //may move into DialogActivity onResume
            }};
        Handler mHandler = new Handler();
        mHandler.postDelayed(r, 2000); //1 second to load activity
    }

    private void showSystemAlert(){
        showNotification();

        Log.v(TAG, "show dialog");

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog);
        dialog.setTitle(GlobalSettings.gDialogHead);

        TextView dialogText = (TextView) dialog.findViewById(R.id.dialogText);
        dialogText.setText(GlobalSettings.gDialogBody);

        Button dialogGoToButton = (Button) dialog.findViewById(R.id.dialogGoToButton);
        dialogGoToButton.setText(GlobalSettings.gDialogGoToButton);

        Button dialogExistButton = (Button) dialog.findViewById(R.id.dialogExistButton);
        dialogExistButton.setText(GlobalSettings.gDialogExistButton);


        dialogGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gSurveyURL));
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
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        long lLastCounterUpdate = sharedPref.getLong(getString(R.string.lastCounterUpdate), 0);
        long nextUpdateDue = lLastCounterUpdate + GlobalSettings.gResetShowCounter;
        long currentTimeMills = System.currentTimeMillis();

        if(currentTimeMills > nextUpdateDue) {
            Log.v(TAG,"reset show counter");

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(getString(R.string.lastCounterUpdate), currentTimeMills);
            editor.putInt(getString(R.string.dialogShownCounter), 0);
            editor.commit();
            return true;
        }
        return false;
    }

    private boolean checkShownCounter(){

        checkShownCounterUpdate();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        int lCounter = sharedPref.getInt(getString(R.string.dialogShownCounter), 0);


        Log.v(TAG, "" + lCounter + " <= " + GlobalSettings.gMaxShowCounter );
        if(lCounter <= GlobalSettings.gMaxShowCounter){
            SharedPreferences.Editor editor = sharedPref.edit();
            int newCounter = lCounter + 1;
            editor.putInt(getString(R.string.dialogShownCounter), newCounter);
            editor.commit();
            return true;
        }
        Log.v(TAG,"shown dialog to much, wait for reset");
        return false;
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

    private void addAppToExceptionList(String sAppName) {
        DatabaseConnector connector = new DatabaseConnector(this);

        connector.insert(sAppName);
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

    private boolean isGPSEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return  (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
}
