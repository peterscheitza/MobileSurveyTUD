package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.HandlerService;

import android.content.Context;
import android.media.AudioManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService.EventDetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 05.06.2015.
 */
public class SoundDetectionService extends HandlerDetectorServiceBase{

    static final public String TAG = "Sound Service";

    AudioManager mAudioManager;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    long getHandlerDelay() {
        return GlobalSettings.gSoundEventWait;
    }

    @Override
    boolean conditionToCheck() {
        return mAudioManager.isMusicActive();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
