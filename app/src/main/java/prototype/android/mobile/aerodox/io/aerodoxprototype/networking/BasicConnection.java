package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Header;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public abstract class BasicConnection implements Connection {
    private static final int SEND = 1;
    private static final int RECIEVE = 2;

    private Handler messageHandler;
    private Thread executor;
    private Map<String, ResponseHandler> rspHandlerMap;

    protected BasicConnection() {
        this.rspHandlerMap = new HashMap<>();

    }

    protected abstract void connectSocket() throws IOException;

    protected abstract void sendAction(JSONObject jsonObject);

    @Override
    public void start() {
        initExecutor();
        executor.start();
    }

    private void initExecutor() {
        executor = new Thread() {

            @Override
            public void run() {
                try {
                    connectSocket();
                    Looper.prepare();
                    messageHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            JSONObject packet = (JSONObject) msg.obj;
                            switch (msg.what) {
                                case SEND:
                                    sendAction(packet);
                                    break;
                                case RECIEVE:
                                    processResponse(packet);
                                    break;
                            }

                        }
                    };
                    Looper.loop();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
                processResponse(buildPsudoCloseResponse());
            }
        };
    }

    @Override
    public void close() {

        messageHandler.getLooper().quit();
        closeConnection();

    }

    private JSONObject buildPsudoCloseResponse() {
        JSONObject rsp = new JSONObject();
        try {
            rsp.put(Config.RESPONSE_KEY, "close");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rsp;
    }

    private void processResponse(JSONObject response) {
        ResponseHandler handler = this.rspHandlerMap.get(response.remove(Config.RESPONSE_KEY));
        handler.handle(response);
    }

    protected abstract void closeConnection();

    @Override
    public void launchAction(JSONObject action) {
        if (this.isConnected()) {
            this.queueMessage(SEND, action);
        }
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

    @Override
    public void attachResponseHandler(Header responseHeader, ResponseHandler handler) {
        this.rspHandlerMap.put(responseHeader.name().toLowerCase(), handler);
    }
}
