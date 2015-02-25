package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import org.json.JSONObject;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public interface Connection {
    void start();
    void close();
    void launchAction(JSONObject action);
}
