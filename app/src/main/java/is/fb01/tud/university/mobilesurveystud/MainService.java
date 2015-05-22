package is.fb01.tud.university.mobilesurveystud;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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

    private Handler mToastHandler;

    PowerManager.WakeLock mWakeLock;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;

    Intent mTouchDetectionService;
    Intent mButtonDetectionService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        mTouchDetectionService = new Intent(this, TouchDetectionService.class);

        mToastHandler = new Handler();


        mPixelBroadcasteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Touch Service");
                handleTouchReceive(intent);
            }
        };
        IntentFilter filterPixel = new IntentFilter(TouchDetectionService.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPixelBroadcasteReceiver,filterPixel);


        mPowerButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Screen");
                handlePowerButton(intent);
            }
        };

        IntentFilter filterOnOff = new IntentFilter();
        filterOnOff.addAction(Intent.ACTION_SCREEN_OFF);
        //filterOnOff.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mPowerButtonReceiver,filterOnOff);


        startService(mTouchDetectionService);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mPixelBroadcasteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mPowerButtonReceiver);

        stopService(mTouchDetectionService);
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

        inactivityDetected();
    }

    private void handlePowerButton(Intent intent){

        Log.v(TAG,"POWER");

        boolean isAlterShown = inactivityDetected();

        //wakeDevice();
        showActivity();

        if(isAlterShown) {

        }
    }

    private boolean inactivityDetected() {
        long activityDuration = mMillsEnd - mMillsStart;

        //reset parameter
        mMillsStart = -1;
        mMillsEnd = -1;

        if (activityDuration > GlobalSettings.gMinUseDuration) {
            Log.v(TAG, "MS: I would show you the Invitation");

            showToast("MS: Please answer survey, bitch!");

            showSystemAlert(); //show dialog

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

    private void showActivity() {
        Intent i = new Intent(this, DialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        Log.v(TAG,"STARTED");
    }

    private void showSystemAlert(){
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
    }
}
