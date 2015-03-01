package prototype.android.mobile.aerodox.io.aerodoxprototype.communication.lan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.Connection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.ResponseHandler;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.Header;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public class LANConnection implements Connection {
    private TCPConnection tcp;
    private UDPConnection udp;

    private static final Set<String> MOTION_CLASSIFIER;
    static {
        MOTION_CLASSIFIER = new HashSet<>();
        addActionsToSet(MOTION_CLASSIFIER, Header.MOVE,
                                           Header.SWIPE,
                                           Header.TOUCH);

    }
    private static void addActionsToSet(Set<String> set, Header... actions) {
        for (Header action: actions) {
            set.add(action.name().toLowerCase());
        }
    }

    public LANConnection(String ip) {
        this.tcp = new TCPConnection(ip);
        this.udp = new UDPConnection(ip);

    }

    @Override
    public void start() {
        this.tcp.start();
        this.udp.start();
    }

    @Override
    public void close() {
        this.tcp.close();
        this.udp.close();
    }

    @Override
    public void attachResponseHandler(Header responseHeader, ResponseHandler handler) {
        this.tcp.attachResponseHandler(responseHeader, handler);
    }

    @Override
    public void launchAction(JSONObject action) {

        try {
            Connection choice = MOTION_CLASSIFIER.contains(action.getString("act"))? udp: tcp;
            choice.launchAction(action);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return this.tcp.isConnected();
    }
}
