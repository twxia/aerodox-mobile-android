package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public abstract class BasicConnection implements Connection {
    private static final int SEND = 1;
    private static final int RECIEVE = 2;

    private Handler messageHandler;
    private Thread executor;

    protected abstract void connectSocket() throws IOException;

    protected abstract void sendAction(JSONObject jsonObject);

    @Override
    public void start() {
        prepareExecutor();
        executor.start();
    }

    private void prepareExecutor() {
        executor = new Thread() {

            @Override
            public void run() {
                try {
                    connectSocket();
                    Looper.prepare();
                    messageHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case SEND:
                                    sendAction((JSONObject) msg.obj);
                                    break;
                                case RECIEVE:
                                    break;
                            }

                        }
                    };
                    Looper.loop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void close() {
        messageHandler.getLooper().quit();
        closeConnection();

    }

    protected abstract void closeConnection();

    @Override
    public void launchAction(JSONObject action) {
        this.queueMessage(SEND, action);
    }

    protected void recieveResponse(JSONObject response) {
        this.queueMessage(RECIEVE, response);
    }

    private void queueMessage(int what, JSONObject json) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = json;

        messageHandler.sendMessage(msg);
    }

}
