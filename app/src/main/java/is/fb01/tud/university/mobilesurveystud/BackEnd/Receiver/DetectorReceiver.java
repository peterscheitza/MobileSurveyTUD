package is.fb01.tud.university.mobilesurveystud.BackEnd.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.MainService;
import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling.ServiceStruct;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;
import is.fb01.tud.university.mobilesurveystud.R;

/**
 * Created by peter_000 on 29.08.2015.
 * Receiver zur Kommunikation der Detektoren mit dem MainService
 *
 * Diese Klasse ist eine Auskapselung aus dem MainService um eine bessere
 * Übersicht zu gewährleisten. Sie Arbeitet eng mit dem MainService zusammen
 * und schreibt seine Ergebnisse in dessen Member.
 *
 * Der mDetectorReceiver ist für das Empfangen der Nachrichten zuständig, die von den Detektoren gesendet werden. Somit ist er
 * der zentrale Kommunikationskanal zwischen den Detektoren und dem MainService. Es handelt sich hierbei um eine Instanz der Klasse
 * DetectorReceiver, welche die Klasse BoradcastReceiver um die benötigte Funktionalität erweitert. Der String
 * “is.fb01.tud.university.mobilesurveystud.MSG” für den IntentFilter liegt statisch der DetectorServiceBase zu Grunde und steht somit
 * jedem Detektor zum Senden von Nachrichten zur Verfügung.
 *
 */
public class DetectorReceiver extends BroadcastReceiver {

    static final String TAG = "DetectorReceiver";

    private MainService mMainService;
    private boolean mIsExtendedRunning = false;

    String mSender, mServiceType;
    boolean mIsActive;
    long mCurrentStart, mCurrentEnd;


    public DetectorReceiver(MainService mainService){
        mMainService = mainService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        readDate(intent);

        Log.v(TAG, "onReceiveMessage from Service: " + mSender + " mIsActive: " + mIsActive);

        updateMainStartEnd(mCurrentStart, mCurrentEnd);

        updateMainServiceMap(mSender, mIsActive);

        updateExtendedDetectors();

        if(!mIsActive)
            mMainService.isShowADialog();
    }

    /**
     * Lesen der im Intent übergeben Daten
     * @param intent daten
     */
    void readDate(Intent intent){
        mSender = intent.getStringExtra(mMainService.getString(R.string.sender));
        mServiceType = intent.getStringExtra(mMainService.getString(R.string.serviceType));
        mIsActive = intent.getBooleanExtra(mMainService.getString(R.string.is_active), false);
        mCurrentStart = intent.getLongExtra(mMainService.getString(R.string.millsStart), -1);
        mCurrentEnd = intent.getLongExtra(mMainService.getString(R.string.millsEnd), -1);
    }

    /**
     * Updated die Zeiten im MainService wenn es benötigit ist
     * @param millsStart
     * @param millsEnd
     */
    private void updateMainStartEnd(long millsStart, long millsEnd){
        if(millsStart > -1)
            if (mMainService.mMillsStart == -1 || millsStart < mMainService.mMillsStart)
                mMainService.mMillsStart = millsStart;

        if(millsEnd > -1)
            if (mMainService.mMillsEnd < millsEnd)
                mMainService. mMillsEnd = millsEnd;
    }

    /**
     * Updaten der  ServiceStructs bezüglich der Aktivität
     *
     * Sollte der aktuelle Service Inaktivität melden wird geprüft ob dieser Service bei Inaktivität
     * abgestellt werden soll. In dem Fall wird der Detektor gestoppt
     * @param sender
     * @param isActive
     */
    private void updateMainServiceMap(String sender, boolean isActive){
        if (MainService.mServiceMap.containsKey(sender)) {
            ServiceStruct struct = MainService.mServiceMap.get(sender);

            struct.isActive = isActive;
            if (struct.checkStop(ServiceStruct.stopSituation.INACTIVITY) && !isActive) {
                mMainService.stopService(struct.mIntent);
                struct.state = GlobalSettings.State.OFF;
            }
        } else {
            Log.v(TAG, "something went wrong. service not found");
            assert false;
        }
    }

    /**
     * Sollten die StandardDetektoren (im Moment nur TouchDetektion) inaktiv sein werden die anderen
     * Detektoren gesetartet um zu prüfen, ob der Nutzer anderweitig interagiert
     */
    private void updateExtendedDetectors(){
        if (isStandardInactivity()) {
            if (!mIsExtendedRunning && mMainService.mIsScreenOn) {
                mMainService.startServiceEvent(ServiceStruct.startSituation.EXTEND);
                mIsExtendedRunning = true;
            }
        }

        boolean isE = isExtendedInactivity();
        boolean isS = isStandardInactivity();
        if(isE && !isS)
            mIsExtendedRunning = false;
    }


    //-----HELPER-----

    /**
     * Prüft ob alle Services die NICHT mit EXTEND markeirt sind inaktiv sind
     * Dies wird als Standard Inaktivität bezeichnet
     * @return bool
     */
    private boolean isStandardInactivity(){
        for ( ServiceStruct serviceStruct : MainService.mServiceMap.values()) {
            if(serviceStruct.state == GlobalSettings.State.ON && !serviceStruct.checkStart(ServiceStruct.startSituation.EXTEND)) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    /**
     * Prüft ob alle Services die mit EXTEND markeirt sind inaktiv sind
     * Dies wird als Extended Inaktivität bezeichnet
     * @return bool
     */
    private boolean isExtendedInactivity(){
        for ( ServiceStruct serviceStruct : MainService.mServiceMap.values()) {
            if(serviceStruct.state == GlobalSettings.State.ON && serviceStruct.checkStart(ServiceStruct.startSituation.EXTEND)) {
                if(serviceStruct.isActive)
                    return false;
            }
        }
        return true;
    }

    private void checkDataReset(){

    }
}
