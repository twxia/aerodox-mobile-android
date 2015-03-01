package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import org.json.JSONObject;

/**
 * Created by maeglin89273 on 2/26/15.
 */
public interface ResponseHandler {
    public abstract void handle(JSONObject rspContent);
}
