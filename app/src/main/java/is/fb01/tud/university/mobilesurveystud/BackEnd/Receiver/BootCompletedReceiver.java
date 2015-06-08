package is.fb01.tud.university.mobilesurveystud.BackEnd.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.R;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;

/**
 * Created by peter_000 on 25.05.2015.
 */
public class BootCompletedReceiver extends BroadcastReceiver{

    static final public String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            handleBootCompleteReceive(context);
        }
    }

    private void handleBootCompleteReceive(Context context){
        Resources r = context.getResources();

        String sharedPrefName = r.getString(R.string.shared_Pref);
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        Log.v(TAG, sharedPrefName);

        String optioneName = r.getString(R.string.setting_is_active);
        String lastSavedState = sharedPref.getString(optioneName, MainService.State.UNDEFINED.toString());
        Log.v(TAG,optioneName);
        Log.v(TAG,lastSavedState);


        if(lastSavedState.equals(MainService.State.ON.toString())) {
            Log.v(TAG, "restart service");
            Intent iMainService = new Intent(context, MainService.class);
            context.startService(iMainService);
        }
        else if(lastSavedState.equals(MainService.State.OFF.toString())) {
            //do nothing
            Log.v(TAG, "service was off, we keep it off");
        }
        else{
            Log.v(TAG, "state undefined" + lastSavedState);
        }
    }
}