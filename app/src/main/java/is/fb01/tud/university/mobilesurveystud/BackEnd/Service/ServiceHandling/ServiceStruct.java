package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;
import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 04.06.2015.
 */
public class ServiceStruct {

    public Intent mIntent;
    List<startSituation> mStartList;
    List<stopSituation> mStopList;
    DetectorServiceBase mDetector;

    public GlobalSettings.State state = GlobalSettings.State.UNDEFINED;
    public boolean isActive = false;

    /**
     * Enumeration der Start- und Stopsituationen.
     *
     * Es muss mindesten eine Start- und eine Stopsituation übergeben werden
     * Der MainService ruft an den entsprechenden Stellen die Situationen auf
     *
     * SCREEN_ON    Bildschirm wird angeschaltet
     * SCREEN_OFF   Bildschirm wird abgeschaltet
     * EXTEND       Die herkömlichen Detektoren haben inaktivität festgestellt
     *
     * NEVER        Platzhalter um zu übergeben, dass der Service nicht beendet werden soll
     * INACTIVITY   Der Service ist inaktiv
     */
    static public enum startSituation {
        SCREEN_ON, SCREEN_OFF, EXTEND
    }
    static public enum stopSituation {
        NEVER, SCREEN_OFF, INACTIVITY
    }

    /**
     * Konstruktoren der ServiceStructs
     *
     * @param intent    Intent zum Starten des Detektor
     * @param start     Startsituationen (einzeln oder als Liste)
     * @param stop      Stopsituationen (einzeln oder als Liste)
     */
    public ServiceStruct(Intent intent, startSituation start, stopSituation stop){
        initCollection();

        mIntent = intent;
        mStartList.add(start);
        mStopList.add(stop);
    }
    public ServiceStruct(Intent intent, List<startSituation> start, stopSituation stop){
        initCollection();

        mIntent = intent;
        mStartList = start;
        mStopList.add(stop);
    }
    public ServiceStruct(Intent intent, startSituation start,  List<stopSituation> stop){
        initCollection();

        mIntent = intent;
        mStartList.add(start);
        mStopList = stop;
    }
    public ServiceStruct(Intent intent,  List<startSituation> start,  List<stopSituation> stop){
        initCollection();

        mIntent = intent;
        mStartList = start;
        mStopList = stop;
    }

    /**
     * Initialisieren der ArrayListen der Start- und Stopsituationen
     */
    private void initCollection(){
        mStartList = new ArrayList<startSituation>();
        mStopList = new ArrayList<stopSituation>();
    }

    /**
     * Prüft ob der Detektor in der entsprechenden Situation starten oder stoppen soll
     *
     * @parm situation zu prüfende Situation
     * @return  boolean
     */
    public boolean checkStop(stopSituation stop){

        for (stopSituation s : mStopList){
            if(s == stop)
                return true;
        }

        return false;
    }

    /**
     * Prüft ob der Detektor in der entsprechenden Situation starten oder stoppen soll
     *
     * @parm situation zu prüfende Situation
     * @return  boolean
     */
    public boolean checkStart(startSituation start){

        for (startSituation s : mStartList){
            if(s == start)
                return true;
        }

        return false;
    }
}
