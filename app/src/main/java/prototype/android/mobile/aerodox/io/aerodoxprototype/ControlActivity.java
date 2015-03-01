package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.GyroSignalEmitter;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.Header;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.Connection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.ConnectionFactory;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostInfo;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.ResponseHandler;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.MouseButtonListener;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.TouchMediator;


public class ControlActivity extends Activity {
    private HostInfo host;
    private Connection actionLauncher;

    private Button btnLeft, btnRight;

    private TouchMediator touchMediator;
    private GyroSignalEmitter gyroEmt;
    private int sensitivity = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = this.getIntent();
        host = (HostInfo) intent.getSerializableExtra("host");

        initActionLauncher(host);

        gyroEmt = new GyroSignalEmitter((SensorManager) getSystemService(SENSOR_SERVICE), actionLauncher);

        btnLeft = (Button) this.findViewById(R.id.btnLeft);
        btnRight = (Button) this.findViewById(R.id.btnRight);

        MouseButtonListener btnListener = new MouseButtonListener(gyroEmt, actionLauncher);
        btnLeft.setOnTouchListener(btnListener);
        btnRight.setOnTouchListener(btnListener);

        SurfaceView touchPad = (SurfaceView) this.findViewById(R.id.touchPad);
        this.touchMediator = new TouchMediator(gyroEmt, actionLauncher);
        touchPad.setOnTouchListener(this.touchMediator);

    }


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
    protected void onResume() {
        super.onResume();
        gyroEmt.unlock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gyroEmt.lock();
    }

}
