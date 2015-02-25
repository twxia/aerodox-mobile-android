package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public class TCPConnection extends BasicConnection {
    private Socket socket;
    private Writer socketWriter;
    private SocketAddress clientIP;
    private boolean startSeperator = false;

    public TCPConnection(String ip) {
        clientIP = new InetSocketAddress(ip, Config.TCP_PORT);
    }

    @Override
    protected void connectSocket() throws IOException {

            socket = new Socket();
            //add socket configuration here
            socket.setSendBufferSize(120);

            socket.connect(clientIP);
            socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            socketWriter.write("[");
    }

    @Override
    protected void sendAction(JSONObject jsonObject) {
        try {
            if (startSeperator) {
                socketWriter.write(",");
            }
            String packet = jsonObject.toString();
            socketWriter.write(packet);
            socketWriter.flush();
            startSeperator = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void closeConnection() {
        try {
            socketWriter.write("]");
            socketWriter.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
