package is.fb01.tud.university.mobilesurveystud.FrontEnd;

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
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Created by peter_000 on 12.06.2015.
 * Sammlung der in der Anwendung benötigten Notifications
 *
 * Der Notifier dient der Bündelung aller Arten von Notifications. Eine Notification dient der Nutzerinformation zu einem
 * Sachverhalt über die Anwendungsgrenzen hinaus und muss dem Betriebssystem übergeben werden.
 */
public class Notifier {

    static String TAG = "Notifier";

    static int mNotificationID = 5;

    private Context mContext;

    /**
     * Konstruktor
     * @param c Context zum Erzeugen der Notification und des Managers
     */
    public Notifier(Context c)
    {
        mContext = c;
    }

    /**
     * Notification zur benachichtigung des Nutzer wenn dieser zur Umfrage aufgefordert wird
     *
     * Es werden der Standardsound und Vibration genutzt
     */
    public void alert(){
        Log.v(TAG, "notification");

        Notification notification  = new Notification.Builder(mContext).build();
        notification.defaults =  Notification.DEFAULT_ALL;

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

        notificationManager.notify(mNotificationID, notification);
        mNotificationID++;
    }

    /**
     * Erzeugt die Notification die beim setzten der Foreground Flag übergeben werden muss
     *
     * Sie ist nur visuell in der Statusleiste sichtbar und führt zur MainActivity
     * @return
     */
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

        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        return notification;
    }
}
