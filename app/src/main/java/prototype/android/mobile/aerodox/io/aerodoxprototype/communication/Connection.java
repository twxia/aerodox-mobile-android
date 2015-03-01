package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import org.json.JSONObject;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controlling.Header;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public interface Connection {
    public abstract void start();
    public abstract void launchAction(JSONObject action);
    public abstract boolean isConnected();
    public abstract void close();
    public abstract void attachResponseHandler(Header responseHeader, ResponseHandler handler);

}
