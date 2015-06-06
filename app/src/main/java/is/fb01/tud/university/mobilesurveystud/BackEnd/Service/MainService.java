package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

    private BroadcastReceiver mLocalBroadcasteReceiver;
    private BroadcastReceiver mGyroBroadcasteReceiver;
    private BroadcastReceiver mPowerButtonReceiver;
    private BroadcastReceiver mDialogReceiver;
    private BroadcastReceiver waitForUnlockReceiver;

    private Handler mToastHandler;

    private PowerManager.WakeLock mWakeLock;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;
    private boolean isScreenOn = true;
    //private boolean mIsHighMovement = false;

    /*private Intent mTouchDetectionService;
    private Intent mAcceleromterService;
    private Intent mGyroService;*/

    State gyroState =State.UNDEFINED;


    //private Intent mButtonDetectionService;

    /*private class ServiceStruct{
        public String TAG;
        public GlobalSettings.ServiceStates state = GlobalSettings.ServiceStates.UNDEFINED;
        public boolean isActive = false;
    }*/

    HashMap<String,ServiceStruct> mStandardServiceMap = new HashMap<>();
    HashMap<String,ServiceStruct> mAdditionalServiceMap = new HashMap<>();

    boolean mIsUseAdditional = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        mToastHandler = new Handler();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        String optioneName = getString(R.string.is_gyro);
        String sGyroState = sharedPref.getString(optioneName, State.UNDEFINED.toString());
        gyroState = State.valueOf(sGyroState);


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

                if(mStandardServiceMap.containsKey(sender))
                    mStandardServiceMap.get(sender).isActive = isActive;
                else if(mAdditionalServiceMap.containsKey(sender))
                    mAdditionalServiceMap.get(sender).isActive = isActive;
                else
                    Log.v(TAG, "something went wrong. service not found"); assert false;

                if(mIsUseAdditional) {
                    if (isStandardInactivity())
                        startAdditionalServices();
                    else
                        stopAdditionalServices();
                }

                isShowADialog();










                /*switch (sender) {
                    case TouchDetectionService.TAG:
                        handleTouchReceive(isActive); break;
                    case GyroscopeService.TAG:
                        handleGyroReceive(isActive); break;
                    default: Log.v(TAG, "wrong receive"); assert false;
                }*/
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

        for ( ServiceStruct serviceStruct : mStandardServiceMap.values()) {
            if(serviceStruct.state == State.ON)
                stopService(serviceStruct.intent);
            serviceStruct.state = State.OFF;
        }

        for ( ServiceStruct serviceStruct : mAdditionalServiceMap.values()) {
            if(serviceStruct.state == State.ON)
                stopService(serviceStruct.intent);
            serviceStruct.state = State.OFF;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    private void initServiceMaps(){

        Intent touchDetectionService = new Intent(this, TouchDetectionService.class);
        Intent buttonDetectionService = new Intent(this, ButtonDetectionService.class);
        Intent soundDetectionService = new Intent(this, SoundDetectionService.class);

        Intent gyroService = new Intent(this, GyroscopeService.class);
        Intent acceleromterService = new Intent(this, AccelerometerService.class);

        mStandardServiceMap.put(TouchDetectionService.TAG, new ServiceStruct(touchDetectionService));
        //mStandardServiceMap.put(ButtonDetectionService.TAG, new ServiceStruct(buttonDetectionService));
        mStandardServiceMap.put(SoundDetectionService.TAG, new ServiceStruct(soundDetectionService));

        mAdditionalServiceMap.put(GyroscopeService.TAG, new ServiceStruct(gyroService));
        //mAdditionalServiceMap.put(AccelerometerService.TAG, new ServiceStruct(acceleromterService));

        for ( ServiceStruct serviceStruct : mStandardServiceMap.values()) {
            startService(serviceStruct.intent);
            serviceStruct.state = State.ON;
        }

        for ( ServiceStruct serviceStruct : mAdditionalServiceMap.values()) {
            serviceStruct.state = State.OFF;
        }
    }

    private void startAdditionalServices() {
        for (ServiceStruct serviceStruct : mAdditionalServiceMap.values()) {
            if (serviceStruct.state == State.OFF) {
                startService(serviceStruct.intent);
                serviceStruct.state = State.ON;
                serviceStruct.isActive = true;
            }
        }

    }

    private void stopAdditionalServices(){
        for ( ServiceStruct serviceStruct : mAdditionalServiceMap.values()) {
            if(serviceStruct.state == State.ON) {
                stopService(serviceStruct.intent);
                serviceStruct.state = State.OFF;
                serviceStruct.isActive = false;
            }
        }
    }

    private void startStandardServices(){
        for ( ServiceStruct serviceStruct : mStandardServiceMap.values()) {
            if(serviceStruct.state == State.OFF) {
                startService(serviceStruct.intent);
                serviceStruct.state = State.ON;
            }
        }
    }

    private void stopStandardServices(){
        for ( ServiceStruct serviceStruct : mStandardServiceMap.values()) {
            if(serviceStruct.state == State.ON) {
                stopService(serviceStruct.intent);
                serviceStruct.state = State.OFF;
                serviceStruct.isActive = false;
            }
        }
    }



    private void handleTouchReceive(boolean isActive) {
        showToast("TD: I think you are inactive");

        mAdditionalServiceMap.get(GyroscopeService.TAG).isActive = isActive;

        if(isActive){

        }
        else {
            if (mIsUseAdditional)
                startAdditionalServices();
            else
                isShowADialog();
        }
    }

    private void handlePowerButton(Intent intent){
        Log.v(TAG,"screen turned off/on");

        if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
            isScreenOn = false;
            stopStandardServices();
            stopAdditionalServices();
            //stopService(mTouchDetectionService);
            //stopService(mGyroService);
        }
        else if(intent.getAction() == Intent.ACTION_SCREEN_ON) {
            isScreenOn = true;
            resetParameter();
            startStandardServices();
            //startService(mTouchDetectionService);
            //startService(mGyroService);
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

    private void handleGyroReceive(boolean isActive) {

        mAdditionalServiceMap.get(GyroscopeService.TAG).isActive = isActive;

        if(isActive) {
            showToast("GD: I think you shaking a lot");
        }
        else {
            stopService(mAdditionalServiceMap.get(GyroscopeService.TAG).intent);

            showToast("GD: I think you are inactive");

            isShowADialog();
        }
    }


    private boolean isInactivity(){
        if(isStandardInactivity() && isAdditionalInactivity())
            return true;
        else
            return false;
    }

    private boolean isStandardInactivity(){
        for ( ServiceStruct serviceStruct : mStandardServiceMap.values()) {
            if(serviceStruct.state == State.ON) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    private boolean isAdditionalInactivity(){
        for ( ServiceStruct serviceStruct : mAdditionalServiceMap.values()) {
            if(serviceStruct.state == State.ON) {
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

                if (isScreenOn)
                    showSystemAlert();
                else
                    showActivity();

                return true;
            }

            resetParameter();
        }
        return false;
    }

    /*private boolean isShowADialog() {
        long activityDuration = mMillsEnd - mMillsStart;

        Log.v(TAG, "activityDuration: " + activityDuration);

        if (activityDuration > GlobalSettings.gMinUseDuration && !mIsHighMovement) {
            showToast("MS: Please answer survey, bitch!");

            resetParameter();

            if(isScreenOn)
                showSystemAlert();
            else
                showActivity();

            return true;
        }
        return false;
    }*/

    private void resetParameter(){
        mMillsStart = -1;
        mMillsEnd = -1;
        //mIsHighMovement = false;
    }

    public void wakeDevice() {

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.PARTIAL_WAKE_LOCK,
                "UselessWakeTag");
        mWakeLock.acquire();

        Log.v(TAG, "WAKEUP");
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
}
