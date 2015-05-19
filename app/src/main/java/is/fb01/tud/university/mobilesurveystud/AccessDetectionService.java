package is.fb01.tud.university.mobilesurveystud;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AccessDetectionService extends AccessibilityService {

    static final String TAG = "AccessibilityService";
    static final public String MSG = "is.fb01.tud.university.mobilesurveystud." + TAG + ".MSG";

    LocalBroadcastManager mBroadcaster;

    private Handler mEventHandler;
    private Runnable mEventRunnable;

    private long mMillsStart;
    private long mMillsEnd;
    private boolean mEventRunning;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
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

                mEventRunning = false;
            }
        };

        mEventRunning=false;

        //"typeViewClicked|typeViewLongClicked|typeViewHoverEnter|typeViewHoverExit|typeViewScrolled"
       /* AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);*/

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG, event.toString());

        //Intent intent = new Intent(UPDATE_LOG);
          //  intent.putExtra(UPDATE_LOG, createOutputText(event));
        //broadcaster.sendBroadcast(intent);

        if(event.getPackageName() != null) { //just to be safe (touch exploration)
            if (!event.getPackageName().equals(MainActivity.PACKAGE_NAME)) { //ignore my own events
                if (mEventRunning) {
                    mEventHandler.removeCallbacksAndMessages(null);
                    mEventHandler.postDelayed(mEventRunnable, GlobalSettings.gAccessEventWait);
                    mMillsEnd = System.currentTimeMillis();
                } else {
                    mEventHandler.postDelayed(mEventRunnable, GlobalSettings.gAccessEventWait);
                    mMillsStart = System.currentTimeMillis();
                    mEventRunning = true;

                    //inform MainService of new events, this is needed to stop pixel detection
                    Log.v(TAG, "Broadcasting new event message");

                    Intent intent = new Intent("Started receiving accessibility events");
                    intent.setAction(MSG);
                    //we do not need start and end time due the post of them while
                    //next end of accessibility events appearance
                    intent.putExtra("millsStart", -1);
                    intent.putExtra("millsEnd", -1);
                    intent.putExtra("isNewAccessEvent", true);
                    mBroadcaster.sendBroadcast(intent);
                }
            }

        }
    }

    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }



    private String createOutputText(AccessibilityEvent event)
    {
        final int eventType = event.getEventType();

        DateFormat df = new SimpleDateFormat("HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        String eventText = date + ": " + eventTypeToString(eventType);

        return  eventText;
    }

    private String eventTypeToString(int eventType)
    {

        switch(eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "TYPE_TOUCH_INTERACTION_END";
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "TYPE_WINDOWS_CHANGED";
            default: return "UNKNOWN";
        }
    }

}
