package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public class TCPConnection extends BasicConnection {
    private Socket socket;
    private Writer socketWriter;
    private BufferedReader socketReader;
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

        socket.connect(clientIP, Config.TIMEOUT);

        initWriterAndReader();

    }

    private void initWriterAndReader() throws IOException {
        socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread readerThread = new Thread() {
            @Override
            public void run() {
                for (;!socket.isClosed();) {
                    try {
                        String jsonLiteral = socketReader.readLine();
                        recieveResponse(new JSONObject(jsonLiteral));
                    } catch (IOException | JSONException | NullPointerException e) {
//                        e.printStackTrace();
                        close();
                    }
                }
            }
        };
        readerThread.start();
    }

    @Override
    protected void sendAction(JSONObject jsonObject) {
        try {

            socketWriter.write(startSeperator? ",": "[");

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
            if (!socket.isOutputShutdown()) {
                socketWriter.write("]");
                socketWriter.flush();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return socket !=null? socket.isConnected(): false;
    }
}
