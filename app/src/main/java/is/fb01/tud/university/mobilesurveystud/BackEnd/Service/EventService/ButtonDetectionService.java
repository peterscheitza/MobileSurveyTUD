package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 22.05.2015.
 */
public class ButtonDetectionService extends EventDetectorServiceBase {

    static final public String TAG = "ButtonService";

    private final int xPixel = 10;
    private final int yPixel = 10;

    private WindowManager mWindowManager;
    //private LinearLayout mTouchLayout;
    private View mTouchLayout;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onServiceConnected");

        mTouchLayout = new onKeyLinearLayout(this);
        mTouchLayout.setLayoutParams(new LinearLayout.LayoutParams(xPixel, yPixel));
        mTouchLayout.setBackgroundColor(Color.CYAN);
        //mTouchLayout.setOnKeyListener(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                xPixel,
                yPixel,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, //Phone!
                //geht: TYPE_PHONE, TYPE_SYSTEM_ALERT
                //geht nicht: TYPE_SYSTEM_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.RIGHT | Gravity.TOP;
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

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /*@Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());

        onEvent();

        togglePixelColor();

        return false;
    }*/

    private void togglePixelColor(){

        if(mMillsEnd-mMillsStart > GlobalSettings.gMinUseDuration)
            mTouchLayout.setBackgroundColor(Color.GREEN);
        else
            mTouchLayout.setBackgroundColor(Color.CYAN);

        mTouchLayout.invalidate();
    }

    private class onKeyLinearLayout extends View {
        onKeyLinearLayout(Context context){
            super(context);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            Log.v(TAG,"dis");
            mWindowManager.removeView(mTouchLayout);
            return super.dispatchKeyEvent(event);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            Log.v(TAG,"down");
            return super.onKeyDown(keyCode, event);
        }

        @Override
        public boolean dispatchGenericMotionEvent (MotionEvent event) {
            Log.v(TAG,"gm");
            return super.dispatchGenericMotionEvent(event);
        }

        @Override
        public boolean dispatchKeyShortcutEvent  (KeyEvent event) {
            Log.v(TAG,"short");
            return super.dispatchKeyShortcutEvent(event);
        }

        /*@Override
        public boolean dispatchTouchEvent  (MotionEvent event) {
            Log.v(TAG,"touch " + event.getAction() + event.getButtonState());

            return super.dispatchTouchEvent (event);
        }*/

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            super.onWindowFocusChanged(hasFocus);

            Log.d("Focus debug", "Focus changed !");

            if (!hasFocus) {
                Log.d("Focus debug", "Lost focus !");

            }
        }

    }
}
