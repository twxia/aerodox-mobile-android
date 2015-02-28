package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ButtonKey;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Header;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.Connection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.ResponseHandler;

/**
* Created by maeglin89273 on 2/23/15.
*/
class ButtonListener implements View.OnTouchListener {

    private Connection actionLauncher;
    private Map<ButtonKey, Long> lastClickTime = new HashMap<ButtonKey, Long>();;

    private SensorManager sensorMgr;
    private Sensor gyro;

    private Activity activity;
    private boolean gyroAction = false;

    ButtonListener(Activity act, Connection actionLauncher) {
        this.actionLauncher = actionLauncher;
        this.activity = act;

        sensorMgr = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
        gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        lastClickTime.put(ButtonKey.LEFT, 0L);
        lastClickTime.put(ButtonKey.RIGHT, 0L);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ButtonKey btnKey = (v.getId() == R.id.btnLeft) ? ButtonKey.LEFT : ButtonKey.RIGHT;

        boolean btnPress;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                btnPress = true;

                break;
            case MotionEvent.ACTION_UP:
                btnPress = false;

                break;
            default:
                return false;
        }

        lastClickTime.put(btnKey, SystemClock.elapsedRealtime());

        if (Math.abs(lastClickTime.get(ButtonKey.LEFT).longValue() - lastClickTime.get(ButtonKey.RIGHT).longValue()) > Config.MAX_CLICK_DURATION){
            actionLauncher.launchAction(ActionBuilder.newAction(Header.BUTTON)
                    .setBtnState(btnKey, btnPress)
                    .getResult());
        }else{
            if(gyroAction) {
                sensorMgr.unregisterListener((android.hardware.SensorEventListener) activity);
                gyroAction = false;
            }else {
                sensorMgr.registerListener((android.hardware.SensorEventListener) activity, gyro, SensorManager.SENSOR_DELAY_GAME);
                gyroAction = true;
            }
        }

        return false;
    }
}
