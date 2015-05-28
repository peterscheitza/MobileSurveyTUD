package is.fb01.tud.university.mobilesurveystud;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public static final String PACKAGE_NAME = "is.fb01.tud.university.mobilesurveystud";

    static final String TAG = "MainActivity";

    Intent mMainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainService = new Intent(this, MainService.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

        /*
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);*/

        Button toggleMainButton = (Button) findViewById(R.id.mainToggleService);
        Button toggleGyroButton = (Button) findViewById(R.id.gyroToggleService);

        if(isServiceRunning(MainService.class))
            toggleMainButton.setText("Stop Service");
        else
            toggleMainButton.setText("Start Service");

        if(isServiceRunning(GyroscopeService.class))
            toggleGyroButton.setText("Stop Gyro Service");
        else
            toggleGyroButton.setText("Start Gyro Service");


    }

    public void buttonToggleService(View v){

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if(isServiceRunning(MainService.class)){
            stopService(mMainService);
            ((Button)v).setText("Start Service");
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.is_active), GlobalSettings.ServiceStates.OFF.toString());
            editor.putString(getString(R.string.is_gyro), GlobalSettings.ServiceStates.OFF.toString());
        }
        else{
            startService(mMainService);
            ((Button)v).setText("Stop Service");
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.is_active), GlobalSettings.ServiceStates.ON.toString());
        }

        editor.commit();


        SharedPreferences sharedPref2 = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);

        String optioneName = getString(R.string.is_active);
        String lastSavedState = sharedPref2.getString(optioneName, GlobalSettings.ServiceStates.UNDEFINED.toString());

        Log.v(TAG,lastSavedState);
    }

    public void buttonToggleGyro(View v){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if(isServiceRunning(GyroscopeService.class)){
            ((Button)v).setText("Start Gyro Service");
            Toast.makeText(this, "Stop Gyro Service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.is_gyro), GlobalSettings.ServiceStates.OFF.toString());
        }
        else{
            startService(mMainService);
            ((Button)v).setText("Stop Gyro Service");
            Toast.makeText(this, "Start Gyro Service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.is_gyro), GlobalSettings.ServiceStates.ON.toString());
        }


        //restart to toggle Gyro
        stopService(mMainService);
        startService(mMainService);

        editor.commit();
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
