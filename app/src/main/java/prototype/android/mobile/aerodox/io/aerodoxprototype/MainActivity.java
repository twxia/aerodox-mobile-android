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
import android.util.FloatMath;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private static final long EXPO = 10000000L;
    InetAddress address;
    int port;

    SocketThread s = new SocketThread();
//    DatagramSocket socket;
    Socket socket;

    int action = 1; // 1=move, 2=swipe
    int[] btnState = new int[]{0, 0}; // [0] 0=nothing, 1=left, 2=middle, 3=right , [1] ispress

    double[] accVec = new double[]{0, 0, 0};
    double[] gyroVec = new double[]{0, 0, 0};
    double[] rotVec = new double[]{0, 0, 0};
    double[] touchStart = new double[]{0, 0};
    double[] touchDelta = new double[]{0, 0};
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
//                socket = new DatagramSocket();
//                socket.connect(address, port);
                socket = new Socket();
                socket.setTcpNoDelay(true);
                socket.setTrafficClass(0x12);
                socket.setSendBufferSize(100);
                socket.setPerformancePreferences(1, 2, 0);
                socket.connect(new InetSocketAddress(address, port));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write("[");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnectSocket() throws IOException {
            writer.write("]");
            socket.close();
        }

        private void writeAction(JSONObject jsonObject) {
            try {

//                byte[] data = jsonObject.toString().getBytes();
//                DatagramPacket packet = new DatagramPacket(data, data.length);
//                socket.send(packet);
            writer.write(jsonObject.toString() + ",");
            writer.flush();
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
                handleGyro(event);
                break;

            default:
                Toast.makeText(getApplicationContext(), "No Sensor Responds", Toast.LENGTH_SHORT).show();
        }


    }

    private float timestamp = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.0000001f;
    private void handleGyro(SensorEvent event) {

        float oldTimestamp = timestamp;
        timestamp = event.timestamp;
        if (oldTimestamp != 0) {

            final float dT = (timestamp - oldTimestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = FloatMath.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude <= EPSILON) {
                return;
            }

            axisX /= omegaMagnitude;
            axisY /= omegaMagnitude;
            axisZ /= omegaMagnitude;

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = FloatMath.sin(thetaOverTwo);
            gyroVec[0] = sinThetaOverTwo * axisX;
            gyroVec[1] = sinThetaOverTwo * axisY;
            gyroVec[2] = sinThetaOverTwo * axisZ;

            s.post();
        }

    }


    private JSONArray compressVecJson(double[] vec) throws JSONException {
        JSONArray compressedJson = new JSONArray();
        for (int i = 0; i < vec.length; i++) {
            compressedJson.put(i, Long.toString((long)(vec[i] * EXPO), 36));
        }
        return  compressedJson;

    }


    private JSONObject makeBtnStateJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("num", btnState[0]);
        obj.put("isPress", btnState[1] == 1? true: false);

        return obj;
    }

    private JSONObject makeActionJson(){
        JSONObject returnJson = new JSONObject();

        if(btnState[0] != 0) {
            try {
                returnJson.put("act", "button");
                returnJson.put("btnState", makeBtnStateJson());
                btnState[0] = 0;
                return returnJson;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (isTouch) {
            try {
                returnJson.put("act", "touch");
                returnJson.put("touchMov", compressVecJson(touchDelta));
                return returnJson;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        switch(action){
            case 1:
                try {
                    returnJson.put("act", "move");
//                    returnJson.put("acc", compressVecJson(accVec));
                    returnJson.put("gyro", compressVecJson(gyroVec));
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

//        sensorMgr.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);
        sensorMgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
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
