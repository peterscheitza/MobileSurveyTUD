package is.fb01.tud.university.mobilesurveystud.BackEnd.Service;

import android.content.Intent;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 04.06.2015.
 */
public class ServiceStruct {

    ServiceStruct(Intent i, MainService.ServiceType eType){
        intent = i;
        type = eType;
    }

    public Intent intent;
    public MainService.ServiceType type;
    public MainService.State state = MainService.State.UNDEFINED;
    public boolean isActive = false;
}
