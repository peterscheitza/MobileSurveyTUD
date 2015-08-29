package is.fb01.tud.university.mobilesurveystud.FrontEnd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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


public class DialogActivity extends Activity {

    static final public String TAG = "DialogActivity";
    static final public String MSG = "is.fb01.tud.university.mobilesurveystud." + TAG + ".MSG";

    LocalBroadcastManager mBroadcaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog);

        mBroadcaster  = LocalBroadcastManager.getInstance(this);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setTitle(getString(R.string.dialog_head));

        TextView dialogText = (TextView) findViewById(R.id.activityText);
        dialogText.setText(getString(R.string.dialog_body));

        Button activityGoToButton = (Button) findViewById(R.id.activityGoToButton);
        activityGoToButton.setText(getString(R.string.dialog_goto_button));

        Button activityExistButton = (Button) findViewById(R.id.activityExitButton);
        activityExistButton.setText(getString(R.string.dialog_exit_button));

        activityGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gSurveyURL));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);*/

                Log.v(TAG, "say Service to open browser on unlock");

                Intent intent = new Intent("Open browser after unlock");
                intent.setAction(MSG);
                mBroadcaster.sendBroadcast(intent);

                finish();
            }
        });

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
    }

    @Override
    public void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

       return (netInfo != null && netInfo.isConnected());
    }
}
