package is.fb01.tud.university.mobilesurveystud;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.FrontEnd.DialogActivity;

/**
 * Created by peter_000 on 12.06.2015.
 */
public class Notifier {

    static String TAG = "Notifier";

    static int mNotificationID = 5;

    private Context mContext;
    Handler mToastHandler;

    public Notifier(Context c){
        mContext = c;
    }

    public void alert(){
        Log.v(TAG, "notification");

        Notification notification  = new Notification.Builder(mContext).build();
        notification.defaults =  Notification.DEFAULT_ALL;

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

        notificationManager.notify(mNotificationID, notification);
        mNotificationID++;
    }
}
