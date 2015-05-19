package is.fb01.tud.university.mobilesurveystud;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class TouchDetectionService extends Service implements OnTouchListener {

    static final public String TAG = "TouchService";
    static final public String MSG = "is.fb01.tud.university.mobilesurveystud." + TAG + ".MSG";

    private final int xPixel = 1;
    private final int yPixel = 1;

    private WindowManager mWindowManager;
    private LinearLayout mTouchLayout;

    LocalBroadcastManager mBroadcaster;

    private Handler mEventHandler;

    private boolean mTouched = false;
    private long mStartTime = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onServiceConnected");

        mBroadcaster  = LocalBroadcastManager.getInstance(this);

        mStartTime = System.currentTimeMillis();

        mEventHandler = new Handler();
        Runnable runner = new Runnable(){
            public void run() {

                Log.v(TAG, "Broadcasting handler message");

                Intent intent = new Intent("No touch detected");
                intent.setAction(MSG);
                intent.putExtra("millsEnd", mStartTime); // nothing detected
                intent.putExtra("isTouched", false);
                mBroadcaster.sendBroadcast(intent);
            }
        };

        mEventHandler.postDelayed(runner, GlobalSettings.gTouchEventWait);

        mTouchLayout = new LinearLayout(this);
        mTouchLayout.setLayoutParams(new LayoutParams(xPixel, yPixel));
        mTouchLayout.setBackgroundColor(Color.CYAN);
        mTouchLayout.setOnTouchListener(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                xPixel,
                yPixel,
                WindowManager.LayoutParams.TYPE_PHONE, //Phone!
                    //geht: TYPE_PHONE, TYPE_SYSTEM_ALERT
                    //geht nicht: TYPE_SYSTEM_OVERLAY
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                //| WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

        mWindowManager.addView(mTouchLayout, mParams);

        Log.i(TAG, "added View");
    }


    @Override
    public void onDestroy() {
        Log.v(TAG, "onServiceDisconnected");

        if(mWindowManager != null) {
            if(mTouchLayout != null) {
                mWindowManager.removeView(mTouchLayout);
                Log.i(TAG, "removed View");
            }
        }

        mEventHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE)
          //  Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());

       if(!mTouched) {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());
            } else {
                Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());
            }

            mTouched = true;

            mEventHandler.removeCallbacksAndMessages(null);

            Log.v(TAG, "Broadcasting touch message");

            Intent intent = new Intent("Detected touch event");
            intent.setAction(MSG);
            intent.putExtra("millsEnd", event.getEventTime());
            intent.putExtra("isTouched", true);
            mBroadcaster.sendBroadcast(intent);
        }

        return false;
    }



}

