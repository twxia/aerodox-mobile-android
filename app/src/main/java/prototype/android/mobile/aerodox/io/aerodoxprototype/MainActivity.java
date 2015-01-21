package prototype.android.mobile.aerodox.io.aerodoxprototype;

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
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    InetAddress address;
    int port;

    SocketThread s = new SocketThread();
    DatagramSocket socket;

    int action = 1; // 1=move, 2=swipe
    int[] btnState = new int[]{0, 0}; // [0] 0=nothing, 1=left, 2=middle, 3=right , [1] ispress

    float[] accVec = new float[]{0, 0, 0};
    float[] gyroVec = new float[]{0, 0, 0};
    float[] rotVec = new float[]{0, 0, 0};
    float[] touchStart = new float[]{0, 0};
    float[] touchDelta = new float[]{0, 0};
    SensorManager sensorMgr;
    Sensor acc, gyro, rot;

    Button btnLeft, btnRight;

    boolean isTouch = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = this.getIntent();
        String ip = intent.getStringExtra("ip");
        try {
            address = InetAddress.getByName(ip);
            port = intent.getIntExtra("port", 1810);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        Log.i("connection", "connect to ip: " + ip);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rot = sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        btnLeft = (Button) this.findViewById(R.id.btnLeft);
        btnRight = (Button) this.findViewById(R.id.btnRight);

        btnLeft.setOnTouchListener(new ButtonListener());
        btnRight.setOnTouchListener(new ButtonListener());

        SurfaceView touchPad = (SurfaceView) this.findViewById(R.id.touchPad);
        touchPad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStart[0] = event.getX();
                        touchStart[1] = event.getY();
                    case MotionEvent.ACTION_MOVE:
                        isTouch = true;
                        touchDelta[0] = event.getX() - touchStart[0];
                        touchDelta[1] = event.getY() - touchStart[1];
                        touchStart[0] = event.getX();
                        touchStart[1] = event.getY();
                        s.post();
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouch = false;
                }

                return true;
            }
        });
        s.start();
    }

    private class ButtonListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    btnState[1] = 1;
                    Log.i("touch", "down");
                    break;
                case MotionEvent.ACTION_UP:
                    btnState[1] = 0;
                    Log.i("touch", "up");
                    break;
            }
            btnState[0] = (v.getId() == R.id.btnLeft) ? 1 : 3;
            s.post();
            return false;
        }
    }

    private class SocketThread extends Thread {

        Writer writer;
        Handler mHandler;

        Runnable launch = new Runnable() {
            @Override
            public void run() {
                if (socket.isConnected()) {
                    writeAction(makeActionJson());
                }else{
                    Log.i("socket", "not connect");
                }

            }
        };

        @Override
        public void run() {
            super.run();
            connectSocket();

            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }

        private void connectSocket() {

            try {
                socket = new DatagramSocket();
                socket.connect(address, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnectSocket() {
                socket.close();
        }

        private void writeAction(JSONObject jsonObject) {
            try {
                byte[] data = jsonObject.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*@Override
        public void interrupt() {
            super.interrupt();
            //disconnectSocket();
        }*/

        void post() {
            if (mHandler != null) {
                mHandler.post(launch);
            }
        }

    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                accVec[0] = event.values[0];
                accVec[1] = event.values[1];
                accVec[2] = event.values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroVec[0] = event.values[0];
                gyroVec[1] = event.values[1];
                gyroVec[2] = event.values[2];
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotVec[0] = event.values[0];
                rotVec[1] = event.values[1];
                rotVec[2] = event.values[2];
                break;
            default:
                Toast.makeText(getApplicationContext(), "No Sensor Responds", Toast.LENGTH_SHORT).show();
        }
        s.post();

    }


    private JSONObject makeAccJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("x", accVec[0]);
        obj.put("y", accVec[1]);
        obj.put("z", accVec[2]);

        return obj;
    }

    private JSONObject makeGyroJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("x", gyroVec[0]);
        obj.put("y", gyroVec[1]);
        obj.put("z", gyroVec[2]);

        return obj;
    }

    private JSONObject makeRotVecJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("x", rotVec[0]);
        obj.put("y", rotVec[1]);
        obj.put("z", rotVec[2]);

        return obj;
    }

    private JSONObject makeBtnStateJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("num", btnState[0]);
        obj.put("isPress", btnState[1] == 1? true: false);

        return obj;
    }

    private JSONObject makePosMovJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("x", touchDelta[0]);
        obj.put("y", touchDelta[1]);

        return obj;
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

        if (isTouch) {
            try {
                returnJson.put("action", "touch");
                returnJson.put("touchMov", makePosMovJson());
                return returnJson;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        switch(action){
            case 1:
                try {
                    returnJson.put("action", "move");
//                    returnJson.put("acc", makeAccJson());
//                    returnJson.put("gyro", makeGyroJson());
                    returnJson.put("rotVec", makeRotVecJson());
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

        //sensorMgr.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);
        //sensorMgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        sensorMgr.registerListener(this, rot, SensorManager.SENSOR_DELAY_GAME);
    }

    /*@Override
    protected void onStop() {
        super.onStop();

        sensorMgr.unregisterListener(this);
        s.interrupt();
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
