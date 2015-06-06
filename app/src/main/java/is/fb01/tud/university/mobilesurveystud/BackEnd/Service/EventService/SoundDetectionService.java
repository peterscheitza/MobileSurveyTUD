package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.content.Context;
import android.media.AudioManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 05.06.2015.
 */
public class SoundDetectionService extends EventDetectorServiceBase implements AudioManager.OnAudioFocusChangeListener {

    static final public String TAG = "Sound Service";

    Context context;

    AudioManager mAudioManager;
    AudioObserver mAudioObserver;

    Handler getFocusBackHandle;

    boolean mIsAudioFocus = false;


    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        runGetFocusHandler();

        mAudioObserver = new AudioObserver(mAudioManager, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mAudioObserver );
    }

    @Override
    public void onDestroy() {

        if(getFocusBackHandle != null)
            getFocusBackHandle.removeCallbacksAndMessages(null);

        getApplicationContext().getContentResolver().unregisterContentObserver(mAudioObserver);

        super.onDestroy();
    }

    private class AudioObserver extends ContentObserver{
        AudioManager mAudioManager;
        int previousStreamVolume;
        int previousNotificationVolume;
        int previousRingVolume;
        int previousSystemVolume;

        public AudioObserver(AudioManager audioManager, Handler handler) {
            super(handler);
            mAudioManager = audioManager;

            previousStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            previousNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            previousRingVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            previousSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            int currentStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int currentNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            int currentRingVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            int currentSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

            if(previousStreamVolume - currentStreamVolume != 0) {
                Log.v(TAG, "adjusted stream volume");
                previousStreamVolume = currentStreamVolume;
                onEvent(GlobalSettings.gSoundEventWait);
                return;
            }

            if(previousNotificationVolume - currentNotificationVolume != 0) {
                Log.v(TAG, "adjusted notification volume");
                previousNotificationVolume = currentNotificationVolume;
                onEvent(GlobalSettings.gSoundEventWait);
                return;
            }

            if(previousRingVolume - currentRingVolume != 0) {
                Log.v(TAG, "adjusted ring volume");
                previousRingVolume = currentRingVolume;
                onEvent(GlobalSettings.gSoundEventWait);
                return;
            }

            if(previousSystemVolume - currentSystemVolume != 0) {
                Log.v(TAG, "adjusted system volume");
                previousSystemVolume = currentSystemVolume;
                onEvent(GlobalSettings.gSoundEventWait);
                return;
            }

        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.v(TAG, "focus changed");

        if(focusChange == AudioManager.AUDIOFOCUS_LOSS){
                //|| focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                //|| focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {  //triggered by notification, results in loop
            onEvent(GlobalSettings.gSoundEventWait);
            runGetFocusHandler();
        }


    }

    private void runGetFocusHandler() {
        getFocusBackHandle = new Handler();
        Runnable runner = new Runnable(){
            public void run() {
                if(mAudioManager.isMusicActive()) {
                    Log.v(TAG,"music still running, try to get focus later");
                    onEvent(GlobalSettings.gSoundEventWait);
                    mIsAudioFocus = false;
                    getFocusBackHandle.postDelayed(this, GlobalSettings.gSoundRequestWait);
                }
                else {
                    if(!mIsAudioFocus) {
                        int result = mAudioManager.requestAudioFocus((AudioManager.OnAudioFocusChangeListener) context,
                                // Use the music stream.
                                AudioManager.STREAM_MUSIC,
                                // Request permanent focus.
                                AudioManager.AUDIOFOCUS_GAIN);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            Log.v(TAG, "got audio focus again");
                            mIsAudioFocus = true;
                        } else {
                            Log.v(TAG, "unable to get  focus");
                            getFocusBackHandle.postDelayed(this, GlobalSettings.gSoundRequestWait);
                        }
                    }
                }

            }
        };
        getFocusBackHandle.postDelayed(runner, GlobalSettings.gSoundRequestWait);
    }
}
