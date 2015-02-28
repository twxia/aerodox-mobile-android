package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Header;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.Connection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.ConnectionFactory;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.HostInfo;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.ResponseHandler;


public class ControlActivity extends Activity implements SensorEventListener {
    private HostInfo host;
    private Connection actionLauncher;

    private SensorManager sensorMgr;
    private Sensor gyro;

    private Button btnLeft, btnRight;

    private TouchMediator touchMediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Intent intent = this.getIntent();
        host = (HostInfo) intent.getSerializableExtra("host");

        initActionLauncher(host);

        btnLeft = (Button) this.findViewById(R.id.btnLeft);
        btnRight = (Button) this.findViewById(R.id.btnRight);

        ButtonListener btnListener = new ButtonListener(this, actionLauncher);
        btnLeft.setOnTouchListener(btnListener);
        btnRight.setOnTouchListener(btnListener);

        SurfaceView touchPad = (SurfaceView) this.findViewById(R.id.touchPad);
        this.touchMediator = new TouchMediator(actionLauncher);
        touchPad.setOnTouchListener(this.touchMediator);

    }

    private int sensitivity = 3;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(sensitivity < Config.MAX_SENSITIVITY)
                        sensitivity++;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                   if(sensitivity > Config.MIN_SENSITIVITY)
                        sensitivity--;
                }
                break;
            default:
                return super.dispatchKeyEvent(event);
        }

        actionLauncher.launchAction(ActionBuilder.newAction(Header.CONFIG)
                .setSensitivity(sensitivity)
                .getResult());

        return true;
    }

    private void initActionLauncher(HostInfo host) {

        actionLauncher = ConnectionFactory.newConnection(host);
        actionLauncher.attachResponseHandler(Header.CONFIG, new ResponseHandler() {

            @Override
            public void handle(JSONObject rspContent) {
                System.out.println(rspContent);
                try {
                    sensitivity = rspContent.getInt("sensitivity");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        actionLauncher.attachResponseHandler(Header.CLOSE, new ResponseHandler() {
            @Override
            public void handle(JSONObject rspContent) {
                System.out.println("Connection is close");
                //popup connection-closed message
            }
        });
        actionLauncher.start();
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
        TouchMediator.Mode mode = this.touchMediator.getMode();
        if (mode == TouchMediator.Mode.TOUCH) {
            return;
        }
        
        double[] gyroVec = new double[3];

        gyroVec[0] = S2REAL_VOL * event.values[0];
        gyroVec[1] = S2REAL_VOL * event.values[1];
        gyroVec[2] = S2REAL_VOL * event.values[2];
        
        Header action = (mode == TouchMediator.Mode.SWIPE)? Header.SWIPE: Header.MOVE;

        actionLauncher.launchAction(ActionBuilder.newAction(action)
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
