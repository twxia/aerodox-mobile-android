package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.UDPConnection;


public class ControlActivity extends Activity implements SensorEventListener {

    private UDPConnection launcher;

    private SensorManager sensorMgr;
    private Sensor gyro;

    private Button btnLeft, btnRight;

    private TouchManager touchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Intent intent = this.getIntent();
        String clientIP = intent.getStringExtra("ip");

        launcher = new UDPConnection(clientIP);
        launcher.start();

        btnLeft = (Button) this.findViewById(R.id.btnLeft);
        btnRight = (Button) this.findViewById(R.id.btnRight);

        btnLeft.setOnTouchListener(new ButtonListener(launcher));
        btnRight.setOnTouchListener(new ButtonListener(launcher));

        SurfaceView touchPad = (SurfaceView) this.findViewById(R.id.touchPad);
        this.touchManager = new TouchManager(launcher);
        touchPad.setOnTouchListener(this.touchManager);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_GYROSCOPE:
                handleGyro(event);
                break;
            default:
                Toast.makeText(getApplicationContext(), "No Sensor Responds", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static final float S2REAL_VOL = 0.02f;
    private void handleGyro(SensorEvent event) {
        TouchManager.Mode mode = this.touchManager.getMode();
        if (mode == TouchManager.Mode.TOUCH) {
            return;
        }
        
        double[] gyroVec = new double[3];

        gyroVec[0] = S2REAL_VOL * event.values[0];
        gyroVec[1] = S2REAL_VOL * event.values[1];
        gyroVec[2] = S2REAL_VOL * event.values[2];
        
        ActionBuilder.Action action = (mode == TouchManager.Mode.SWIPE)? ActionBuilder.Action.SWIPE: ActionBuilder.Action.MOVE;


        launcher.launch(ActionBuilder.newAction(action)
                                     .setGyroVec(gyroVec)
                                     .getResult());
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorMgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();

        sensorMgr.unregisterListener(this);
    }

}
