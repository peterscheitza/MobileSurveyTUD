package is.fb01.tud.university.mobilesurveystud;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

/**
 * Created by peter_000 on 10.05.2015.
 */
public class MainService extends Service {

    static final String TAG = "MainService";

    private BroadcastReceiver mPixelBroadcasteReceiver;
    private BroadcastReceiver mPowerButtonReceiver;
    private BroadcastReceiver mDialogReceiver;
    private BroadcastReceiver waitForUnlockReceiver;

    private Handler mToastHandler;

    private PowerManager.WakeLock mWakeLock;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;

    private boolean isScreenOn = true;

    private Intent mTouchDetectionService;
    private Intent mAcceleromterService;
    Intent mButtonDetectionService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        mTouchDetectionService = new Intent(this, TouchDetectionService.class);
        mAcceleromterService = new Intent(this, AccelerometerService.class);

        mToastHandler = new Handler();


        mPixelBroadcasteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Touch Service: " + intent.toString());
                handleTouchReceive(intent);
            }
        };
        IntentFilter filterPixel = new IntentFilter(TouchDetectionService.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPixelBroadcasteReceiver, filterPixel);


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


        startService(mTouchDetectionService);
        startService(mAcceleromterService);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //LocalBroadcastManager.getInstance(this)....
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mPixelBroadcasteReceiver);
        unregisterReceiver(this.mPowerButtonReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDialogReceiver);

        stopService(mTouchDetectionService);
        stopService(mAcceleromterService);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void handleTouchReceive(Intent intent) {
        showToast("TD: I think you are inactive");

        mMillsStart = intent.getLongExtra("millsStart", -1);
        mMillsEnd = intent.getLongExtra("millsEnd", -1);

        assert mMillsStart != -1;
        assert mMillsEnd != -1;

        if(isShowADialog()) {
            if(isScreenOn)
                showSystemAlert();
            else
                showActivity();
        }
    }

    private void handlePowerButton(Intent intent){
        Log.v(TAG,"screen turned off/on");

        if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
            isScreenOn = false;
            stopService(mTouchDetectionService);
        }
        else if(intent.getAction() == Intent.ACTION_SCREEN_ON) {
            isScreenOn = true;
            startService(mTouchDetectionService);
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



    private boolean isShowADialog() {
        long activityDuration = mMillsEnd - mMillsStart;

        //reset parameter
        mMillsStart = -1;
        mMillsEnd = -1;

        Log.v(TAG, "activityDuration: " +activityDuration);

        if (activityDuration > GlobalSettings.gMinUseDuration) {
            showToast("MS: Please answer survey, bitch!");

            return true;
        }

        return false;
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
