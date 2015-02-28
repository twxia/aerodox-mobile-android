package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.Map;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Header;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.Connection;

/**
 * Created by maeglin89273 on 2/28/15.
 */
public class GyroSignalEmitter implements SensorEventListener {

    public enum EmittingMode {MOVE, SWIPE, STOP}

    private static final Map<EmittingMode, Header> HEADER_MAPPER;
    static {
        HEADER_MAPPER = new HashMap<>();
        HEADER_MAPPER.put(EmittingMode.MOVE, Header.MOVE);
        HEADER_MAPPER.put(EmittingMode.SWIPE, Header.SWIPE);
    }

    private static final float S2REAL_VOL = 0.02f;
    private SensorManager sensorMgr;
    private Sensor gyro;
    private Connection actionLauncher;

    private volatile EmittingMode mode;
    private volatile boolean locked;

    public GyroSignalEmitter(SensorManager sensorMgr, Connection actionLauncher) {
        this.sensorMgr = sensorMgr;
        this.gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.actionLauncher = actionLauncher;

        this.unlock();
        this.setEmmittingMode(EmittingMode.MOVE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_GYROSCOPE:
                emit(event);

        }
    }

    public void lock() {

        sensorMgr.unregisterListener(this, gyro);
        this.locked = true;
    }

    public void unlock() {

        sensorMgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        this.locked = false;
    }


    public boolean isLocked() {
        return this.locked;
    }

    public void setEmmittingMode(EmittingMode mode) {
        if (isLocked()) {
            return;
        }

        this.mode = mode;
    }

    public EmittingMode getMode() {
        return this.mode;
    }

    private void emit(SensorEvent event) {
        if (this.mode == EmittingMode.STOP) {
            return;
        }

        double[] gyroVec = new double[3];

        gyroVec[0] = S2REAL_VOL * event.values[0];
        gyroVec[1] = S2REAL_VOL * event.values[1];
        gyroVec[2] = S2REAL_VOL * event.values[2];

        actionLauncher.launchAction(ActionBuilder.newAction(HEADER_MAPPER.get(this.mode))
                .setGyroVec(gyroVec)
                .getResult());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
}
