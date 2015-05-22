package is.fb01.tud.university.mobilesurveystud;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
    private Runnable mEventRunnable;

    private long mMillsStart = -1;
    private long mMillsEnd = -1;
    private boolean mIsEventRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onServiceConnected");

        mBroadcaster  = LocalBroadcastManager.getInstance(this);

        mEventHandler = new Handler();
        mEventRunnable = new Runnable(){
            public void run() {

                Log.v(TAG, "Broadcasting handler message");

                Intent intent = new Intent("Stopped receiving accessibility events");
                intent.setAction(MSG);
                intent.putExtra("millsStart", mMillsStart);
                intent.putExtra("millsEnd", mMillsEnd);
                intent.putExtra("isNewAccessEvent", false);
                mBroadcaster.sendBroadcast(intent);

                mIsEventRunning = false;
            }
        };


        mTouchLayout = new LinearLayout(this);
        mTouchLayout.setLayoutParams(new LayoutParams(xPixel, yPixel));
        mTouchLayout.setBackgroundColor(Color.CYAN);
        mTouchLayout.setOnTouchListener(this);
        //mTouchLayout.setOnKeyListener(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                xPixel,
                yPixel,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, //Phone!
                    //geht: TYPE_PHONE, TYPE_SYSTEM_ALERT
                    //geht nicht: TYPE_SYSTEM_OVERLAY
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                //| WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.softInputMode =  WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;

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

        Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());

        if (mIsEventRunning) {
            mEventHandler.removeCallbacksAndMessages(null);
            mEventHandler.postDelayed(mEventRunnable, GlobalSettings.gAccessEventWait);
            mMillsEnd = System.currentTimeMillis();
        } else {
            mEventHandler.postDelayed(mEventRunnable, GlobalSettings.gAccessEventWait);
            mMillsStart = System.currentTimeMillis();
            mMillsEnd = System.currentTimeMillis();
            mIsEventRunning = true;
        }

        return false;
    }

    /*@Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.v(TAG,"back");
            //return true;
        }
        else  if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.v(TAG,"home");
            //return true;
        }
        else  if (keyCode == KeyEvent.KEYCODE_SETTINGS) {
            Log.v(TAG,"settings");
            //return true;
        }
        else  if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            Log.v(TAG,"switch");
            //return true;
        }
        else
        {
            Log.v(TAG,event.toString());
        }

        return true;
    }*/
}

