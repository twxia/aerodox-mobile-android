package prototype.android.mobile.aerodox.io.aerodoxprototype.controlling;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xia on 2/21/15.
 */
public class ActionBuilder {

    private static final long EXPO = 10000000L;

    private Map<String, Object> buildingAction;

    private ActionBuilder(Header action) {
        this.buildingAction = new HashMap<>();
        this.buildingAction.put("act", action.name().toLowerCase());
    }

    public static ActionBuilder newAction(Header action) {
        return new ActionBuilder(action);
    }

    public ActionBuilder setTouchMove(double[] touchDelta) {
        this.buildingAction.put("touchMov", makeCompressedVecJson(touchDelta));
        return this;
    }


    public ActionBuilder setBtnState(ButtonKey btnKey, boolean isPress) {
        this.buildingAction.put("btnState", makeBtnStateJson(btnKey, isPress));
        return this;
    }


    public ActionBuilder setGyroVec(double[] gyroVec) {
        this.buildingAction.put("gyro", makeCompressedVecJson(gyroVec));
        return this;
    }


    public ActionBuilder setDirection(String direction) {
        this.buildingAction.put("dir", direction);
        return this;
    }


    public ActionBuilder setSensitivity(int level) {
        this.buildingAction.put("sensitivity", level);
        return this;
    }

    private static JSONArray makeCompressedVecJson(double[] vec) {
        JSONArray compressedJson = new JSONArray();
        try {
            for (int i = 0; i < vec.length; i++) {
                compressedJson.put(i, Long.toString((long)(vec[i] * EXPO), 36));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  compressedJson;
    }

    private static JSONObject makeBtnStateJson(ButtonKey btntKey, boolean isPress) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("num", btntKey.getValue());
            obj.put("isPress", isPress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }


    public JSONObject getResult() {
        return new JSONObject(this.buildingAction);
    }

}
