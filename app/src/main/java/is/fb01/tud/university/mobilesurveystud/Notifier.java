package is.fb01.tud.university.mobilesurveystud;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
import is.fb01.tud.university.mobilesurveystud.FrontEnd.MainActivity;

/**
 * Created by peter_000 on 12.06.2015.
 */
public class Notifier {

    static String TAG = "Notifier";

    static int mNotificationID = 5;

    private Context mContext;

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

    public Notification getForgroundNotification(){

        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification  = new Notification.Builder(mContext)
                .setContentIntent(contentIntent)
                .setContentTitle("MobileSurveysTUD")
                .setSmallIcon(R.drawable.ms_tud)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.athena))
                .build();

        return notification;
    }
}
