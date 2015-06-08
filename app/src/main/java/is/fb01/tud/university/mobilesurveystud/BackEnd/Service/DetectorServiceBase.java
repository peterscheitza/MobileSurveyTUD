package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Created by peter_000 on 04.06.2015.
 */
public abstract class DetectorServiceBase extends Service {

    public static final String Package = "is.fb01.tud.university.mobilesurveystud";
    public static final String MSG = Package + ".MSG";

    protected boolean mServiceStopSelf = true;

    protected long mMillsStart = -1;
    protected long mMillsEnd = -1;
    protected boolean isActive = false;

    private LocalBroadcastManager mBroadcaster;

    public abstract String getTag();
    public abstract String getServiceType();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(getTag(), "onServiceConnected");

        Notification notification  = new Notification.Builder(this).build();
        startForeground(1,notification);

        resetParamter();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(getTag(), "onServiceDisconnected");

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void sendBroadcast(String TAG){
        Log.v(TAG, "Broadcasting handler message");

        Intent intent = new Intent("Stopped receiving accessibility events");
        intent.setAction(MSG);
        intent.putExtra(getString(R.string.sender), getTag());
        intent.putExtra(getString(R.string.serviceType), getServiceType());
        intent.putExtra(getString(R.string.millsStart), mMillsStart);
        intent.putExtra(getString(R.string.millsEnd), mMillsEnd);
        intent.putExtra(getString(R.string.is_active), isActive);
        mBroadcaster.sendBroadcast(intent);
    };

    protected void resetParamter(){
        isActive = false;
        mMillsStart = -1;
        mMillsEnd = -1;
    }
}
