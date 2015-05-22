package is.fb01.tud.university.mobilesurveystud;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;


public class DialogActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setTitle(GlobalSettings.gDialogHead);

        TextView dialogText = (TextView) findViewById(R.id.ActivityText);
        dialogText.setText(GlobalSettings.gDialogBody);

        Button activityGoToButton = (Button) findViewById(R.id.ActivityGoToButton);
        activityGoToButton.setText(GlobalSettings.gDialogGoToButton);

        Button activityExistButton = (Button) findViewById(R.id.ActivityExistButton);
        activityExistButton.setText(GlobalSettings.gDialogExistButton);

        activityGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(GlobalSettings.gSurveyURL));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        activityExistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //WebView activityWebView = (WebView) findViewById(R.id.ActivityWebView);
        //activityWebView.loadUrl(GlobalSettings.gSurveyURL);
    }

    @Override
    public void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
}
