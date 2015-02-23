package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionFormat;

/**
 * Created by xia on 2/21/15.
 */
public class UDPConnection extends Thread {
    Handler mHandler;
    DatagramSocket socket;
    InetAddress clientIP;

    public UDPConnection(String IP){
        try {
            clientIP = InetAddress.getByName(IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        connectSocket();

        Looper.prepare();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                try {
                    writeAction(ActionFormat.makeActionJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Looper.loop();
    }

    private void connectSocket() {

        try {
            socket = new DatagramSocket();
            socket.setSendBufferSize(120);
            socket.connect(clientIP, Config.UDP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAction(JSONObject jsonObject) {
        System.out.println(jsonObject.toString());

        try {
            byte[] data = jsonObject.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void post() {
        mHandler.sendEmptyMessage(0);
    }

}
