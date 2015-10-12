package is.fb01.tud.university.mobilesurveystud.FrontEnd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Die DialogActivity dient der Erinnerung des Nutzers an die Umfrage und wird entweder als Dialogfenster oder bildschirmfüllende
 * Activity angezeigt. Das Anzeigen der Acitivity auf dem kompletten Bildschirm erfolgt, wenn das System zuvor gesperrt wurde.
 * Diese Unterscheidung wird benötigt, da es nicht möglich ist ein Dialogfenster über dem Sperrbildschirm anzuzeigen. Beide Arten
 * des Aufrufes der DialogActivity erfolgen durch den MainService, wenn dieser ein Informieren des Nutzers feststellt.
 */
public class DialogActivity extends Activity {

    static final public String TAG = "DialogActivity";
    static final public String MSG = "is.fb01.tud.university.mobilesurveystud." + TAG + ".MSG";

    Context mContext;

    LocalBroadcastManager mBroadcaster;

    Handler mEventHandlerDis;
    Runnable mEventRunnableDis;

    Handler mEventHandlerRem;
    Runnable mEventRunnableRem;

    /**
     * Setzen der Beschriftungen und der Listener der Activity
     *
     * Wenn es in den GlobalSettings eingestellt wurde wird auch die WebView angezeigt
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_dialog);

        mBroadcaster  = LocalBroadcastManager.getInstance(this);

        setTitle(getString(R.string.dialog_head));

        TextView dialogText = (TextView) findViewById(R.id.activityText);
        dialogText.setText(getString(R.string.dialog_body));

        Button activityGoToButton = (Button) findViewById(R.id.activityGoToButton);
        activityGoToButton.setText(getString(R.string.dialog_goto_button));

        Button activityExistButton = (Button) findViewById(R.id.activityExitButton);
        activityExistButton.setText(getString(R.string.dialog_exit_button));

        /**
         * Sendet ein Intent an de mDialogReceiver im MainService
         * Dieser Intent informiert den MainService, dass der Nutzer die Umfrage beantworten möchte
         */
        activityGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "say Service to open browser on unlock");

                Intent intent = new Intent("Open browser after unlock");
                intent.setAction(MSG);
                mBroadcaster.sendBroadcast(intent);

                finish();
            }
        });

        /**
         * Beendet die Umfrage
         */
        activityExistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        WebView activityWebView = (WebView) findViewById(R.id.activityWebView);

        if(GlobalSettings.gIsShowWebView && isOnline()) {


            activityWebView.getSettings().setJavaScriptEnabled(true);
            activityWebView.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.v(TAG, "Oh no! " + description);
                }
            });

            activityWebView.loadUrl(GlobalSettings.gGetURLWithID(this));

        }
        else {
            activityWebView.setVisibility(View.GONE);
        }

        new Notifier(this).alert();

        mEventHandlerRem = new Handler();
        mEventRunnableRem = new Runnable(){
            public void run() {
                new Notifier(mContext).alert();
            }
        };
        mEventHandlerRem.postDelayed(mEventRunnableRem, GlobalSettings.gDismissDialog / 2);

        mEventHandlerDis = new Handler();
        mEventRunnableDis = new Runnable(){
            public void run() {
                finish();
            }
        };
        mEventHandlerDis.postDelayed(mEventRunnableDis, GlobalSettings.gDismissDialog);
    }

    /**
     * Setzt die Flags zum Anzeigen der Activity auf dem Sperrbildschirm
     */
    @Override
    public void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    /**
     * Prüft, ob eine Internetverbindung besteht
     * @return
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

       return (netInfo != null && netInfo.isConnected());
    }
}
