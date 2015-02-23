package prototype.android.mobile.aerodox.io.aerodoxprototype.controling;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xia on 2/21/15.
 */
public class ActionFormat {

    // int action
    //
    // 0=config, 1=move, 2=swipe, 3=touch, 4=button

    private static Action action = Action.CONFIG;

    // int[] pressState = new int[]{0, 0}
    //
    // [0]:(press type)
    // 0=nothing(touchpad), 1=left, 2=middle, 3=right
    //
    // [1]:(press state)
    // 0=nothing, 1=press

    private static ButtonState btnState;

    private static double[] gyroVec = new double[]{0, 0, 0};
    private static double[] touchStart = new double[]{0, 0};
    private static double[] touchDelta = new double[]{0, 0};
    private static final long EXPO = 10000000L;

    public static void setTouchStart(double[] touchStart) {
        ActionFormat.touchStart = touchStart;
    }

    public static void setTouchDelta(double[] touchDelta) {
        ActionFormat.touchDelta = touchDelta;
    }

    public static double[] getTouchDelta() {
        return touchDelta;
    }

    public static double[] getTouchStart() {
        return touchStart;
    }

    public static void setAction(Action action) {
        ActionFormat.action = action;
    }

    public static void setBtnState(ButtonKey bntKey, boolean isPress) {
        ActionFormat.btnState.key = bntKey;
        ActionFormat.btnState.press = isPress;
    }

    public static void setGyroVec(double[] gyroVec) {
        ActionFormat.gyroVec = gyroVec;
    }

    private static JSONArray compressVecJson(double[] vec) throws JSONException {
        JSONArray compressedJson = new JSONArray();
        for (int i = 0; i < vec.length; i++) {
            compressedJson.put(i, Long.toString((long)(vec[i] * EXPO), 36));
        }
        return  compressedJson;
    }

    public static JSONObject makeBtnStateJson() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("num", ActionFormat.btnState.key.getValue());
        obj.put("isPress", ActionFormat.btnState.press);

        return obj;
    }

    public static JSONObject makeActionJson() throws JSONException {
        JSONObject returnJson = new JSONObject();

        switch(action){
            case MOVE:
                returnJson.put("act", "move");
                returnJson.put("gyro", compressVecJson(gyroVec));
                break;
            case SWIPE:
                returnJson.put("act", "swipe");
                returnJson.put("gyro", compressVecJson(gyroVec));
                break;
            case BUTTON:
                returnJson.put("act", "button");
                returnJson.put("btnState", makeBtnStateJson());
                break;
            case TOUCH:
                returnJson.put("act", "touch");
                returnJson.put("touchMov", compressVecJson(touchDelta));
        }

        return returnJson;
    }
}
