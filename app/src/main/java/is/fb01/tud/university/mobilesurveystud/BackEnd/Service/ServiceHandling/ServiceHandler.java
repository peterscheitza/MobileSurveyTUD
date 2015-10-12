package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.ServiceHandling;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import is.fb01.tud.university.mobilesurveystud.BackEnd.Service.DetectorServiceBase;

/**
 * Created by peter_000 on 30.08.2015.
 * Erweitert die HaspMap<String, ServiceStruct> und die Funktionalität zum speichern von ServiceStructs
 */
public class ServiceHandler extends HashMap<String,ServiceStruct> {

    Context mContext;

    public ServiceHandler(Context context){
        mContext = context;
    }

    /**
     * Speichern eines ServiceStructs
     * @param detector Name des Detektor
     * @param start eine Startsituation
     * @param stop eine Stopsituation
     */
    public void put(DetectorServiceBase detector, ServiceStruct.startSituation start, ServiceStruct.stopSituation stop){
        Intent intent = new Intent(mContext, detector.getClass());
        ServiceStruct serviceStruct = new ServiceStruct(intent, start, stop);
        this.put(detector.getTag(), serviceStruct);
    }

    /**
     * Speichern eines ServiceStructs
     * @param detector Name des Detektor
     * @param start mehrere Startsituationen
     * @param stop eine Stopsituation
     */
    public void put(DetectorServiceBase detector, List<ServiceStruct.startSituation> start, ServiceStruct.stopSituation stop){
        Intent intent = new Intent(mContext, detector.getClass());
        ServiceStruct serviceStruct = new ServiceStruct(intent, start, stop);
        this.put(detector.getTag(), serviceStruct);
    }

    /**
     * Speichern eines ServiceStructs
     * @param detector Name des Detektor
     * @param start eine Startsituation
     * @param stop mehrere Stopsituationen
     */
    public void put(DetectorServiceBase detector, ServiceStruct.startSituation start,  List<ServiceStruct.stopSituation> stop){
        Intent intent = new Intent(mContext, detector.getClass());
        ServiceStruct serviceStruct = new ServiceStruct(intent, start, stop);
        this.put(detector.getTag(), serviceStruct);
    }

    /**
     * Speichern eines ServiceStructs
     * @param detector Name des Detektor
     * @param start mehrere Startsituationen
     * @param stop mehrere Stopsituationen
     */
    public void put(DetectorServiceBase detector,  List<ServiceStruct.startSituation> start,  List<ServiceStruct.stopSituation> stop){
        Intent intent = new Intent(mContext, detector.getClass());
        ServiceStruct serviceStruct = new ServiceStruct(intent, start, stop);
        this.put(detector.getTag(), serviceStruct);
    }
}
