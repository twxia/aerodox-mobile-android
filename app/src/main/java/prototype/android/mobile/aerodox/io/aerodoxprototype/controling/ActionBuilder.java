package prototype.android.mobile.aerodox.io.aerodoxprototype.controling;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xia on 2/21/15.
 */
public abstract class ActionBuilder {

    /**
     * Created by xia on 2/22/15.
     */
    public enum Action {
        CONFIG,
        MOVE,
        TOUCH,
        BUTTON,
        SWIPE
    }

    // int[] pressState = new int[]{0, 0}
    //
    // [0]:(press type)
    // 0=nothing(touchpad), 1=left, 2=middle, 3=right
    //
    // [1]:(press state)
    // 0=nothing, 1=press

    public static ActionBuilder newAction(Action action) {
        return new ActionBuilderImpl(action);
    }

    public abstract ActionBuilder setTouchMove(double[] touchDelta);
    public abstract ActionBuilder setBtnState(ButtonKey bntKey, boolean isPress);
    public abstract ActionBuilder setGyroVec(double[] gyroVec);
    public abstract JSONObject getResult();

    private static class ActionBuilderImpl extends ActionBuilder {
        private static final long EXPO = 10000000L;

        private Map<String, Object> buildAction;

        private ActionBuilderImpl(Action action) {
            this.buildAction = new HashMap<>();
            this.buildAction.put("act", action.name().toLowerCase());
        }

        @Override
        public ActionBuilder setTouchMove(double[] touchDelta) {
            this.buildAction.put("touchMov", compressVecJson(touchDelta));
            return this;
        }

        @Override
        public ActionBuilder setBtnState(ButtonKey btnKey, boolean isPress) {
            this.buildAction.put("btnState", makeBtnStateJson(btnKey, isPress));
            return this;
        }

        @Override
        public ActionBuilder setGyroVec(double[] gyroVec) {
            this.buildAction.put("gyro", compressVecJson(gyroVec));
            return this;
        }

        private static JSONArray compressVecJson(double[] vec) {
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

        @Override
        public JSONObject getResult() {
            return new JSONObject(this.buildAction);
        }
    }
}
