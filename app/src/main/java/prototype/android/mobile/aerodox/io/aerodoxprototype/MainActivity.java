package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    String ip = "";
    final int port = 1810;

    SocketThread s = new SocketThread();
    Socket socket;

    int action = 1; // 1=move, 2=swipe
    int[] btnState = new int[]{0, 0}; // [0] 0=nothing, 1=left, 2=middle, 3=right , [1] ispress

    double[] accVec = new double[]{0, 0, 0};
    double[] gyroVec = new double[]{0, 0, 0};
    double[] angleVec = new double[]{0, 0, 0};

    SensorManager sensorMgr;
    Sensor acc, gyro, angle;

    Button btnLeft, btnRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = this.getIntent();
        ip = intent.getStringExtra("ip");

        Log.i("connection", "connect to ip: " + ip);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        angle = sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        btnLeft = (Button) this.findViewById(R.id.btnLeft);
        btnRight = (Button) this.findViewById(R.id.btnRight);

        btnLeft.setOnTouchListener(new ButtonListener());
        btnRight.setOnTouchListener(new ButtonListener());

        s.start();
    }

    private class ButtonListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    btnState[1] = 1;
                    break;
                case MotionEvent.ACTION_UP:
                    btnState[1] = 0;
                    break;
            }
            btnState[0] = (v.getId() == R.id.btnLeft) ? 1 : 2;
            return false;
        }
    }

    private class SocketThread extends Thread {

        Writer writer;

        @Override
        public void run() {
            super.run();

            Looper.prepare();
            final Handler mHandler = new Handler();


            connectSocket(ip, port);

            Runnable launch = new Runnable() {
                @Override
                public void run() {
                    if (socket.isConnected()) {
                        writeAction(makeActionJson());
                    }else{
                        Log.i("socket", "not connect");
                    }
                    mHandler.postDelayed(this, 20);
                }
            };

            mHandler.postDelayed(launch, 50);
            Looper.loop();
        }

        private void connectSocket(String ipString, int port) {
            InetAddress address;

            try {
                address = InetAddress.getByName(ipString);
                socket = new Socket(address, port);

                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write("[");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnectSocket() {
            try {
                writer.write("]");
                writer.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeAction(JSONObject jsonObject) {
            try {
                writer.write(jsonObject.toString() + ",");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            disconnectSocket();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                accVec[0] = (double) event.values[0];
                accVec[1] = (double) event.values[1];
                accVec[2] = (double) event.values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroVec[0] = (double) event.values[0];
                gyroVec[1] = (double) event.values[1];
                gyroVec[2] = (double) event.values[2];
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                angleVec[0] = (double) event.values[0];
                angleVec[1] = (double) event.values[1];
                angleVec[2] = (double) event.values[2];
                break;
            default:
                Toast.makeText(getApplicationContext(), "No Sensor Responds", Toast.LENGTH_SHORT).show();
        }
    }


    private JSONObject makeAccJson() {
        Map map = new HashMap();

        map.put("x", accVec[0]);
        map.put("y", accVec[1]);
        map.put("z", accVec[2]);

        return new JSONObject(map);
    }

    private JSONObject makeGyroJson() {
        Map map = new HashMap();

        map.put("x", gyroVec[0]);
        map.put("y", gyroVec[1]);
        map.put("z", gyroVec[2]);

        return new JSONObject(map);
    }

    private JSONObject makeAngleJson() {
        Map map = new HashMap();

        map.put("x", angleVec[0]);
        map.put("y", angleVec[1]);
        map.put("z", angleVec[2]);

        return new JSONObject(map);
    }

    private JSONObject makeBtnStateJson() {
        Map map = new HashMap();

        map.put("num", btnState[0]);
        map.put("isPress", btnState[1]);

        return new JSONObject(map);
    }

    private JSONObject makeActionJson(){
        JSONObject returnJson = new JSONObject();

        if(btnState[0] != 0) {
            try {
                returnJson.put("action", "button");
                returnJson.put("btnState", makeBtnStateJson());
                btnState[0] = 0;
                return returnJson;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        switch(action){
            case 1:
                try {
                    returnJson.put("action", "move");
                    returnJson.put("gyro", makeGyroJson());
                    returnJson.put("angle", makeAngleJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

        return returnJson;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorMgr.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorMgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorMgr.registerListener(this, angle, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        sensorMgr.unregisterListener(this);
        s.interrupt();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
