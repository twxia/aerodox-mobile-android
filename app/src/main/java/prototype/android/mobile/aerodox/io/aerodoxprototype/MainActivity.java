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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private static final long EXPO = 10000000L;
    InetAddress address;
    int port;

    SocketThread s = new SocketThread();
    DatagramSocket socket;
//    Socket socket;

    int action = 1; // 1=move, 2=swipe
    int[] btnState = new int[]{0, 0}; // [0] 0=nothing, 1=left, 2=middle, 3=right , [1] ispress

    double[] gyroVec = new double[]{0, 0, 0};
    double[] rotVec = new double[]{0, 0, 0};
    double[] touchStart = new double[]{0, 0};
    double[] touchDelta = new double[]{0, 0};
    SensorManager sensorMgr;
    Sensor gyro, rot;

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


        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyro = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        rot = sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


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
/*                        touchStart[0] = event.getX();
                        touchStart[1] = event.getY();
                    case MotionEvent.ACTION_MOVE:*/

                        isTouch = true;
/*                        touchDelta[0] = event.getX() - touchStart[0];
                        touchDelta[1] = event.getY() - touchStart[1];
                        touchStart[0] = event.getX();
                        touchStart[1] = event.getY();*/
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

//        Writer writer;
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
                socket.setSendBufferSize(120);
                socket.connect(address, port);

/*                socket = new Socket();
                socket.setTcpNoDelay(true);
                socket.setTrafficClass(0x12);
                socket.connect(new InetSocketAddress(address, port));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write("[");*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnectSocket() throws IOException {
//            writer.write("]");
            socket.close();
        }

        private void writeAction(JSONObject jsonObject) {
            try {

            byte[] data = jsonObject.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.send(packet);

//            writer.write(jsonObject.toString() + ",");
//            writer.flush();
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

            /*case Sensor.TYPE_ROTATION_VECTOR:
                handleRotVec(event);
                break;
*/
            case Sensor.TYPE_GYROSCOPE:
                handleGyro(event);
                break;
            default:
                Toast.makeText(getApplicationContext(), "No Sensor Responds", Toast.LENGTH_SHORT).show();
        }


    }

    private void handleRotVec(SensorEvent event) {
        for (int i = 0; i < rotVec.length; i++) {
            rotVec[i] = event.values[i];
        }
        s.post();
    }

    private static final float S2REAL_VOL = 0.02f;

    private void handleGyro(SensorEvent event) {



        gyroVec[0] = S2REAL_VOL * event.values[0];
        gyroVec[1] = S2REAL_VOL * event.values[1];
        gyroVec[2] = S2REAL_VOL * event.values[2];
        s.post();
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
                /*returnJson.put("act", "touch");
                returnJson.put("touchMov", compressVecJson(touchDelta));
                return returnJson;*/
                returnJson.put("act", "swipe");
                returnJson.put("gyro", compressVecJson(gyroVec));
                return returnJson;

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        switch(action){
            case 1:
                try {
                    returnJson.put("act", "move");
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

//        sensorMgr.registerListener(this, rot, SensorManager.SENSOR_DELAY_GAME);
        sensorMgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        sensorMgr.unregisterListener(this);

    }*/

    @Override
    protected void onStop() {
        super.onStop();

        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
