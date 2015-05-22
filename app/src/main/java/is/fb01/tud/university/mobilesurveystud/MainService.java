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

    private BroadcastReceiver mPixelBroadcasteReceiver;

    private Handler mToastHandler;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;

    Intent mTouchDetectionService;

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

        startService(mTouchDetectionService);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mPixelBroadcasteReceiver);

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

    private void inactivityDetected() {
        long activityDuration = mMillsEnd - mMillsStart;

        if (activityDuration > GlobalSettings.gMinUseDuration) {
            Log.v(TAG, "MS: I would show you the Invitation");

            showToast("MS: Please answer survey, bitch!");

            showSystemAlert(); //show dialog
        }

        //reset parameter
        mMillsStart = -1;
        mMillsEnd = -1;
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
