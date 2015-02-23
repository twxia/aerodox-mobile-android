package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Action;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionFormat;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ButtonKey;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.UDPConnection;


public class ControlActivity extends Activity implements SensorEventListener {

    UDPConnection launcher;

    SensorManager sensorMgr;
    Sensor gyro;

    Button btnLeft, btnRight;

    long startClickTime = 0;

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

        btnLeft.setOnTouchListener(new ButtonListener());
        btnRight.setOnTouchListener(new ButtonListener());

        SurfaceView touchPad = (SurfaceView) this.findViewById(R.id.touchPad);
        touchPad.setOnTouchListener(new TouchListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ActionFormat.setTouchStart(new double[]{event.getX(), event.getY()});

                    startClickTime = Calendar.getInstance().getTimeInMillis();

                    ActionFormat.setAction(Action.BUTTON);
                    ActionFormat.setBtnState(ButtonKey.LEFT, true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    processMoving(event.getX(), event.getY());
                    launcher.post();

                    break;
                case MotionEvent.ACTION_UP:
                    if(Calendar.getInstance().getTimeInMillis() - startClickTime < Config.MAX_CLICK_DURATION) {
                        ActionFormat.setAction(Action.BUTTON);
                        launcher.post();
                        ActionFormat.setBtnState(ButtonKey.LEFT, false);
                        launcher.post();
                    }
            }

            return true;
        }
    }

    private void processMoving(double x, double y) {
        double[] delta = ActionFormat.getTouchDelta();
        if(delta[0] - x > Config.MOVE_THRESHOLD ||
                delta[1] - y > Config.MOVE_THRESHOLD)
            ActionFormat.setAction(Action.SWIPE);
        else
            ActionFormat.setAction(Action.TOUCH);

        double[] start = ActionFormat.getTouchStart();
        delta[0] = x - start[0];
        delta[1] = y - start[1];
        ActionFormat.setTouchDelta(delta);
    }

    private class ButtonListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ButtonKey btnKey = (v.getId() == R.id.btnLeft) ? ButtonKey.LEFT : ButtonKey.RIGHT;
            boolean btnPress = false;

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    btnPress = true;
                    break;
                case MotionEvent.ACTION_UP:
                    btnPress = false;
                    break;
            }

            ActionFormat.setAction(Action.BUTTON);
            ActionFormat.setBtnState(btnKey, btnPress);

            return false;
        }
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

    private void handleGyro(SensorEvent event) {
        final float S2REAL_VOL = 0.02f;
        double[] gyroVec = new double[3];

        gyroVec[0] = S2REAL_VOL * event.values[0];
        gyroVec[1] = S2REAL_VOL * event.values[1];
        gyroVec[2] = S2REAL_VOL * event.values[2];

        ActionFormat.setGyroVec(gyroVec);
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
