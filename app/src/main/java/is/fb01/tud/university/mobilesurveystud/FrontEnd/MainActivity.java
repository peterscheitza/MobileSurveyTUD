package is.fb01.tud.university.mobilesurveystud.FrontEnd;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.R;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.SensorService.GyroscopeService;


public class MainActivity extends Activity {

    public static final String PACKAGE_NAME = "is.fb01.tud.university.mobilesurveystud";

    static final String TAG = "MainActivity";

    private Context mContext;

    private SharedPreferences mSharedPref;
    private Intent mMainService;

    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mSharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        MainService.State useMainService = readEnum(R.string.setting_is_additional);
        MainService.State useAdditional = readEnum(R.string.setting_is_additional);
        MainService.State useGyro = readEnum(R.string.setting_is_gyro);

        SharedPreferences.Editor editor = mSharedPref.edit();

        //set default value on first init
        if(useMainService == MainService.State.UNDEFINED) {
            editor.putString(getString(R.string.setting_is_active), MainService.State.ON.toString());
            useMainService = MainService.State.ON;
        }

        if(useAdditional == MainService.State.UNDEFINED)
            editor.putString(getString(R.string.setting_is_additional), MainService.State.ON.toString());

        if(useGyro == MainService.State.UNDEFINED)
            editor.putString(getString(R.string.setting_is_gyro), MainService.State.ON.toString());

        editor.commit();



        mMainService = new Intent(this, MainService.class);

        if(!isServiceRunning(MainService.class) && useMainService == MainService.State.ON )
            startService(mMainService);

        Intent intent = new Intent(this, MainService.class);
        mAlarmIntent = PendingIntent.getService(this, 0, intent, 0);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);



        //Front End
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker1);
        int arrayRange = GlobalSettings.gMaxIdleHours - GlobalSettings.gMinIdleHours + 1;
        String[] nums = new String[arrayRange];
        for(int i=0; i<nums.length; i++)
            nums[i] = Integer.toString(GlobalSettings.gMinIdleHours+i);

        np.setMinValue(GlobalSettings.gMinIdleHours);
        np.setMaxValue(GlobalSettings.gMaxIdleHours);
        np.setWrapSelectorWheel(true);
        np.setDisplayedValues(nums);
        np.setValue(GlobalSettings.gMinIdleHours);

        Button toggleGyroButton = (Button) findViewById(R.id.gyroToggleService);
        Button toggleAdditionalButton = (Button) findViewById(R.id.additionalToggleService);

        toggleGyroButton.setVisibility(View.INVISIBLE);
        toggleAdditionalButton.setVisibility(View.INVISIBLE);

        toggleGyroButton.setClickable(false);
        toggleAdditionalButton.setClickable(false);
    }


  /*  @Override
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
    }*/

    @Override
    public void onResume(){
        super.onResume();

        /*
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);*/

        updateFrontEndText();
    }

    public void buttonToggleService(View v){

        if(isServiceRunning(MainService.class)){
            showStopDialog();
        }
        else{
            startService(mMainService);
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();

            //cancel any restart demands - paranoia
            try {
                mAlarmManager.cancel(mAlarmIntent); //clear for safety reasons
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
            }

            updateFrontEndText();

            //editor.putString(getString(R.string.setting_is_active), MainService.State.ON.toString()); happens in service
        }
    }

    public void buttonToggleGyro(View v){
        MainService.State useGyro = readEnum(R.string.setting_is_gyro);

        SharedPreferences.Editor editor = mSharedPref.edit();

        if(useGyro == MainService.State.OFF){
            Toast.makeText(this, "Allowed gyro service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_gyro), MainService.State.ON.toString());
        }
        else if (useGyro == MainService.State.ON) {
            Toast.makeText(this, "Removed gyro service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_gyro), MainService.State.OFF.toString());
        }
        else {
            Toast.makeText(this, "Allowed gyro service", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_gyro), MainService.State.ON.toString());

            Log.v(TAG, "wrong service state");
            assert false;
        }

        updateFrontEndText();

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
            Toast.makeText(this, "Allowed additional services", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_additional), MainService.State.ON.toString());
        }
        else if (useAdditional == MainService.State.ON) {
            Toast.makeText(this, "Removed additional services", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_additional), MainService.State.OFF.toString());
        }
        else {
            Toast.makeText(this, "Allowed additional services", Toast.LENGTH_SHORT).show();

            editor.putString(getString(R.string.setting_is_additional), MainService.State.ON.toString());

            Log.v(TAG, "wrong service state");
            assert false;
        }

        updateFrontEndText();

        if(isServiceRunning(MainService.class)){
            stopService(mMainService);
            startService(mMainService);
        }

        editor.commit();
    }

    public void buttonSaveId(View v){

        SharedPreferences.Editor editor = mSharedPref.edit();

        EditText edittext = (EditText) findViewById(R.id.editText);
        String sId = edittext.getText().toString();

        if(!sId.equals("") && sId != null && !sId.isEmpty()) {
            editor.putString(getString(R.string.user_id), sId);
            editor.commit();

            Toast.makeText(this, "Saved ID: " + sId , Toast.LENGTH_SHORT).show();
        }
    }

    public void buttonGoIdle(View v) {

        if(isServiceRunning(MainService.class)) {

            NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker1);
            long lChoosenNumb = np.getValue();
            long lIdleTime = lChoosenNumb * 1000 * 60 ;//* 60;

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(getString(R.string.setting_is_active), MainService.State.OFF.toString());
            editor.commit();


            try {
                mAlarmManager.cancel(mAlarmIntent); //clear for safety reasons
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
            }

            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lIdleTime, mAlarmIntent);

            stopService(mMainService);

            Log.v(TAG, "going idle on user demand for: " + lIdleTime);

            Toast.makeText(this, "Pausiere Umfrage für " + lChoosenNumb + " Stunden", Toast.LENGTH_SHORT).show();

            updateFrontEndText();
        }
        else {
            Toast.makeText(this, "Umfrage ist bereits pausiert", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateFrontEndText(){
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

        String sId = mSharedPref.getString(getString(R.string.user_id), "");
        EditText edittext = (EditText) findViewById(R.id.editText);
        edittext.setText(sId);
    }

    private void showStopDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle("Umfrage-App dauerhaft stoppen");
        dialogBuilder.setMessage("Sind Sie sicher, dass sie die Umfrageerinnerungen dauerhaft abstellen wollen? Sie müssen die Errinerungen wieder manuel anschalten");

        dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "Stop Service", Toast.LENGTH_SHORT).show();

                        SharedPreferences.Editor editor = mSharedPref.edit();
                        editor.putString(getString(R.string.setting_is_active), MainService.State.OFF.toString());
                        editor.commit();

                        stopService(mMainService);

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                updateFrontEndText();
                            }};

                        Handler mHandler = new Handler();
                        mHandler.postDelayed(r, 2000); //wait for slow devices until main is stopped - paranoia - looks better
                    }
                });

        dialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.show();
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
