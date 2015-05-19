package is.fb01.tud.university.mobilesurveystud;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
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

    private BroadcastReceiver mAccessBroadcasteReceiver;
    private BroadcastReceiver mPixelBroadcasteReceiver;

    private Handler mToastHandler;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;

    Intent mTouchDetectionService;
    private boolean mIsTouchDetection = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onServiceConnected");

        mTouchDetectionService = new Intent(this, TouchDetectionService.class);

        mToastHandler = new Handler();


        mAccessBroadcasteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Accessibility Service");
                handleAccessReceive(intent);
            }
        };
        IntentFilter filterAccess = new IntentFilter(AccessDetectionService.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mAccessBroadcasteReceiver,filterAccess);

        mPixelBroadcasteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG,"onReceiveMessage from Touch Service");
                handleTouchReceive(intent);
            }
        };
        IntentFilter filterPixel = new IntentFilter(TouchDetectionService.MSG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPixelBroadcasteReceiver,filterPixel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mAccessBroadcasteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mPixelBroadcasteReceiver);

        if(mIsTouchDetection)
            stopService(mTouchDetectionService);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void handleAccessReceive(Intent intent) {

        boolean isNewAccessEvent = intent.getBooleanExtra("isNewAccessEvent", true);  //In default we want not throw to much

        if(isNewAccessEvent && mIsTouchDetection)
            handleAccessStart();
        else if(!isNewAccessEvent && !mIsTouchDetection)
            handleAccessEnd(intent);
        else
            Log.v(TAG, "Ignore AccessSevice MSG");
    }

    private void handleAccessStart() {
        if(mIsTouchDetection) {
            Log.v(TAG, "Access event while touch detection");
            //Here we need to stop the touch detection
            //AccessService detected nothing -> TouchDetectService started -> new AccessEvent

            Log.v(TAG, "Attempt to stop TouchDetectionService");
            stopService(mTouchDetectionService);
            mIsTouchDetection = false;
        }
        else {
            Log.v(TAG, "Access event ignored. no touch detection");
        }
    }

    private void handleAccessEnd(Intent intent) {
        //showToast("AS: I think you are inactive");

        if(!mIsTouchDetection) {

            Log.v(TAG, "Attempt to start TouchDetectionService");
            startService(mTouchDetectionService);

            if (mMillsStart == -1) //only update if not set jet
                mMillsStart = intent.getLongExtra("millsStart", -1);
            mMillsEnd = intent.getLongExtra("millsEnd", -1);

            assert mMillsStart != -1;
            assert mMillsEnd != -1;

            mIsTouchDetection = true;
        }
        else {
            Log.v(TAG, "This should not happen");
        }

    }

    private void handleTouchReceive(Intent intent){
        Log.v(TAG, "Attempt to stop TouchDetectionService");
        stopService(mTouchDetectionService);
        mIsTouchDetection = false;

        long newMillsEnd = intent.getLongExtra("millsEnd", -1);
        assert  newMillsEnd != -1;
        mMillsEnd = newMillsEnd;

        boolean isTouched = intent.getBooleanExtra("isTouched",true); //In default we want not throw to much

        if(!isTouched) {
            //showToast("TD: I think you are inactive");
            inactivityDetected();
        }
        //else means continue listening to AccessDetection

        //TODO CASE: no access events after on touch
    }

    private void inactivityDetected() {
        long activityDuration = mMillsEnd - mMillsStart;

        if (activityDuration > GlobalSettings.gMinUseDuration) {
            Log.v(TAG, "MS: I would show you the Invitation");

            showToast("MS: Please answer survey, bitch!");

            showSystemAlert();

            //reset parameter
            mMillsStart = -1;
            mMillsEnd = -1;
            mIsTouchDetection = false; //just to be sure
        }
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
