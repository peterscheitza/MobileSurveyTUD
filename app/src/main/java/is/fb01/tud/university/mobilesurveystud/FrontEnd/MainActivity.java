package is.fb01.tud.university.mobilesurveystud.FrontEnd;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.R;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService.GyroscopeService;


public class MainActivity extends ActionBarActivity {

    public static final String PACKAGE_NAME = "is.fb01.tud.university.mobilesurveystud";

    static final String TAG = "MainActivity";

    SharedPreferences mSharedPref;
    Intent mMainService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);

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
        Button toggleAdditionalButton = (Button) findViewById(R.id.additionalToggleService);

        if(isServiceRunning(MainService.class))
            toggleMainButton.setText("Stop Service");
        else
            toggleMainButton.setText("Start Service");

        MainService.State useGyro = readEnum(R.string.setting_is_gyro);
        if(useGyro == MainService.State.ON)
            toggleGyroButton.setText("Do not use gyro service");
        else
            toggleGyroButton.setText("Use gyro service");


        MainService.State useAdditional = readEnum(R.string.setting_is_additional);
        if(useAdditional == MainService.State.ON)
            toggleAdditionalButton.setText("Do not use additional service");
        else
            toggleAdditionalButton.setText("Use additional services");



    }

    public void buttonToggleService(View v){

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if(isServiceRunning(MainService.class)){
            stopService(mMainService);
            ((Button)v).setText("Start Service");
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_active), MainService.State.OFF.toString());
            editor.putString(getString(R.string.setting_is_gyro), MainService.State.OFF.toString());
            editor.putString(getString(R.string.setting_is_additional), MainService.State.OFF.toString());
        }
        else{
            startService(mMainService);
            ((Button)v).setText("Stop Service");
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_active), MainService.State.ON.toString());
        }

        editor.commit();
    }

    public void buttonToggleGyro(View v){
        MainService.State useGyro = readEnum(R.string.setting_is_gyro);

        SharedPreferences.Editor editor = mSharedPref.edit();

        if(useGyro == MainService.State.OFF){
            ((Button)v).setText("Do not use gyro service");
            Toast.makeText(this, "Allowed gyro service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_gyro), MainService.State.ON.toString());
        }
        else if (useGyro == MainService.State.ON) {
            ((Button)v).setText("Use gyro service");
            Toast.makeText(this, "Removed gyro service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_gyro), MainService.State.OFF.toString());
        }
        else {
            ((Button)v).setText("Do not use gyro service");
            Toast.makeText(this, "Allowed gyro service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_gyro), MainService.State.ON.toString());

            Log.v(TAG, "wrong service state");
            assert false;
        }

        if(isServiceRunning(MainService.class)){
            stopService(mMainService);
            startService(mMainService);
        }

        editor.commit();
    }

    public void buttonToggleAdditional(View v){
        MainService.State useAdditional = readEnum(R.string.setting_is_additional);

        SharedPreferences.Editor editor = mSharedPref.edit();

        if(useAdditional == MainService.State.OFF){
            ((Button)v).setText("Do not use additional service");
            Toast.makeText(this, "Allowed additional services", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_additional), MainService.State.ON.toString());
        }
        else if (useAdditional == MainService.State.ON) {
            ((Button)v).setText("Use additional services");
            Toast.makeText(this, "Removed additional services", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_additional), MainService.State.OFF.toString());
        }
        else {
            ((Button)v).setText("Do not use additional service");
            Toast.makeText(this, "Allowed additional services", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_additional), MainService.State.ON.toString());

            Log.v(TAG, "wrong service state");
            assert false;
        }

        if(isServiceRunning(MainService.class)){
            stopService(mMainService);
            startService(mMainService);
        }

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

    private MainService.State readEnum(int iName){
        String optioneName = getString(iName);
        String sEnum = mSharedPref.getString(optioneName, MainService.State.UNDEFINED.toString());
        return MainService.State.valueOf(sEnum);
    }
}
