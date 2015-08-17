package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

public class TouchDetectionService extends EventDetectorServiceBase implements OnTouchListener {

    static final public String TAG = "TouchService";

    private final int xPixel = 10;
    private final int yPixel = 10;

    private WindowManager mWindowManager;
    private LinearLayout mTouchLayout;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
    long getWaitTime() {
        return GlobalSettings.gTouchEventWait;
    }


    @Override
    public void onDestroy() {
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());

        onEvent();

        togglePixelColor();

        return false;
    }

    private void togglePixelColor(){

        if(mMillsEnd-mMillsStart > GlobalSettings.gMinUseDuration)
            mTouchLayout.setBackgroundColor(Color.GREEN);
        else
            mTouchLayout.setBackgroundColor(Color.CYAN);

        mTouchLayout.invalidate();
    }
}

