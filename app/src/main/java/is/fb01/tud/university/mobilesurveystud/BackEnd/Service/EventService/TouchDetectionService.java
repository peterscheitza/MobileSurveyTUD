package is.fb01.tud.university.mobilesurveystud.BackEnd.Service.EventService;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 01.06.2015.
 * Detektor zur Erfassung der Bildschirmberührung. Nach einigem Ausprobieren zeigte sich,
 * dass alle Berührungen bis auf der der unteren Steuertasten und der des oberen Menüs
 * (runter sliden) erfasst werden. Unabhängig von der gerade aktiven Anwendung
 *
 * Zur Erfassung von Berührungen nutzt der Service ein LinearLayout , welches beim Start des Service erstellt wird. Dieses LinearLayout
 * wird etwas zweckentfremdet genutzt. Um die Funktionsweise komplett zu verstehen werden im Folgenden die Punkte LinearLayout,
 * OnTouchListener und WindowsManager detailliert herausgearbeitet. Ein LinearLayout ist eine Ableitung der Klasse View und kann somit
 * als rechteckiges Objekt auf dem Display dargestellt werden. Dabei ist das LinearLayout 1x1 Pixel groß und hat eine durchsichtige
 * Hintergrundfarbe. Es erhält als OnTouchListener den TouchDetectionService. Dieser imple-mentiert das Interface OnTouchListener und
 * kann, nach der Definition von onTouch(...), dem LinearLayout als Listener übergeben werden. Die Methode onTouch(..) wird immer
 * aufgerufen, wenn eine Berührung des LinearLayouts erfolgt. Der WindowManager  ist ein Systemservice, der für die Verwaltung der
 * zuständig ist. Er gibt vor welche Fenster gerade angezeigt werden und wie dies geschehen soll. Um die Eigenschaften der Darstellung
 * festzulegen, wird jedes View-Objekt mit einem Parameterobjekt übergeben, das alle relevanten Daten enthält. In dem Fall des
 * TouchDetectionService wird ein WindowManager.LayoutParams-Objekt  genutzt.
 */
public class TouchDetectionService extends EventDetectorServiceBase implements OnTouchListener {

    static final public String TAG = "TouchService";

    private final int xPixel = 10;
    private final int yPixel = 10;

    private WindowManager mWindowManager;
    private LinearLayout mTouchLayout;

    /**
     *
     * @return Identifikator des Detektor
     */
    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Als erstes wird der WindowManger Inizialisiert um später die Erfassung zu starten
     *
     * Dann wird das linear Layout mit der größe von einem Pixel und einem transparenten Hitnergrund erzeugt
     *
     * An diesem wird als TouchListener dieser Service übergeben (beachtet: implements OnTouchListener & onTouch(..))
     *
     * Darauf folgend werden die Paramter betreffend des Anzeigen des LinearLayouts festgelegt.
     * - Es hat die Größe 1x1
     * - Es wird als SystemAlert immer oben angezeigt (benötigt Permission)
     * - Es achtete auf Berührungen des Dispaly außerhalb seiner eigenen Fläche
     * - Alle Events die nicht bearbeitet werden können werden an unten liegend Anwendungen weiter geleitet
     * - Das Layout kann keinen Focus erhalten und leitete somit alle seine Eingaben weiter
     * Es soll sich im oberen linken Bereich des Display aufhalten und von der Software-tastatur verschoben werden
     *
     * Darauf folgend wird das Layout dem Manager übergebn und somit angezeigt (aber ist nicht sichtbar)
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mTouchLayout = new LinearLayout(this);
        mTouchLayout.setLayoutParams(new LayoutParams(xPixel, yPixel));
        mTouchLayout.setBackgroundColor(Color.TRANSPARENT);

        mTouchLayout.setOnTouchListener(this);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                xPixel,
                yPixel,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    //geht: TYPE_PHONE, TYPE_SYSTEM_ALERT
                    //geht nicht: TYPE_SYSTEM_OVERLAY
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.softInputMode =  WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;

        mWindowManager.addView(mTouchLayout, mParams);

        Log.i(TAG, "added View");

    }

    /**
     *
     * @return Dauer nach der Inaktivität angenommen wird
     */
    @Override
    long getWaitTime() {
        return GlobalSettings.gTouchEventWait;
    }

    /**
     * Entfernen des layouts vor dem Beenden
     */
    @Override
    public void onDestroy() {
        if(mWindowManager != null) {
            if(mTouchLayout != null) {
                mWindowManager.removeView(mTouchLayout);
                Log.i(TAG, "removed View");
            }
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Funktion die Aufgerufen wird wenn eine Berührung des Displays erfolgt.
     * Darauf hin wird Aktivität angenommen.
     *
     * @param v Berührtes Objekt
     * @param event Art der Berührung
     * @return wurde das Event hier behandelt oder soll es weiter gegeben werden (immer weiter geben)
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i(TAG, "Action :" + event.getAction() + "\t X :" + event.getRawX() + "\t Y :" + event.getRawY());

        onEvent();

        //togglePixelColor();

        return false;
    }


    //nice for debugging, shows when min use duration is reached for this detektor
    private void togglePixelColor(){

        if(mMillsEnd-mMillsStart > GlobalSettings.gMinUseDuration)
            mTouchLayout.setBackgroundColor(Color.GREEN);
        else
            mTouchLayout.setBackgroundColor(Color.CYAN);

        mTouchLayout.invalidate();
    }
}

