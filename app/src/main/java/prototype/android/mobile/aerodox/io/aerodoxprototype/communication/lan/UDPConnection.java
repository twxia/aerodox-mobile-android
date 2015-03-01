package prototype.android.mobile.aerodox.io.aerodoxprototype.communication.lan;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.BasicConnection;

/**
 * Created by xia on 2/21/15.
 */
public class UDPConnection extends BasicConnection {
    private DatagramSocket socket;
    private SocketAddress clientIP;

    public UDPConnection(String address){
        super(address);
    }

    @Override
    protected void connectSocket() throws SocketException {

        socket = new DatagramSocket();
        socket.setSendBufferSize(120);
        socket.connect(new InetSocketAddress(this.address, Config.UDP_PORT));

    }

    @Override
    protected void sendAction(JSONObject jsonObject) {
        try {
            byte[] data = jsonObject.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.send(packet);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    @Override
    protected void closeConnection() {
        socket.close();
    }

    @Override
    public boolean isConnected() {
        return socket !=null? socket.isConnected(): false;
    }
}
