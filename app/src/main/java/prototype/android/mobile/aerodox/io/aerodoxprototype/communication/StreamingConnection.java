package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by maeglin89273 on 3/1/15.
 */
public abstract class StreamingConnection extends BasicConnection {
    private StreamingSocket socket;
    private Writer socketWriter;
    private BufferedReader socketReader;
    private boolean startSeparator = false;

    protected StreamingConnection(String address) {
        super(address);
    }

    @Override
    public void connectSocket() throws IOException {
        StreamingSocket socket = this.connectAsStreamingSocket();
        this.setSocketAndStreams(socket);
    }
    protected abstract StreamingSocket connectAsStreamingSocket() throws IOException;
    private void setSocketAndStreams(StreamingSocket sSocket) throws IOException {
        this.socket = sSocket;
        socketWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        socketReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        Thread readerThread = new Thread() {
            @Override
            public void run() {
                try {
                    for (;!socket.isClosed();) {
                        String jsonLiteral = socketReader.readLine();
                        receiveResponse(new JSONObject(jsonLiteral));
                    }
                } catch (IOException | JSONException | NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    close();
                }

            }
        };
        readerThread.start();
    }

    @Override
    protected void sendAction(JSONObject jsonObject) {
        try {

            socketWriter.write(startSeparator ? ",": "[");

            String packet = jsonObject.toString();
            socketWriter.write(packet);
            socketWriter.flush();
            startSeparator = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void closeConnection() {
        try {
            socketWriter.write("]");
            socketWriter.flush();
        } catch (IOException e) {
//            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null? socket.isConnected(): false;
    }
}
