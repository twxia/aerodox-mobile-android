package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.view.MotionEvent;
import android.view.View;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ButtonKey;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.UDPConnection;

/**
* Created by maeglin89273 on 2/23/15.
*/
class TouchManager implements View.OnTouchListener {
    public enum Mode {TOUCH, SWIPE, NONE};
    long startClickTime = 0;
    private UDPConnection launcher;
    private final TouchModel model;
    private volatile Mode mode;

    TouchManager(UDPConnection launcher) {
        this.launcher = launcher;
        this.model = new TouchModel();
        this.mode = Mode.NONE;
    }

    public Mode getMode() {
        return this.mode;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDown(event.getEventTime(), event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                handleMove(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
               handleUp(event.getEventTime());

        }

        return true;
    }

    private void handleDown(long timestamp, float x, float y) {
        model.setStart(x, y);
        startClickTime = timestamp;
        this.mode = Mode.SWIPE;
    }

    private void handleMove(float x, float y) {

        double distanceSquare = model.moving(x, y);
        if(distanceSquare > Config.MOVE_THRESHOLD) {
            this.mode = Mode.TOUCH;
            launcher.launch(ActionBuilder.newAction(ActionBuilder.Action.TOUCH)
                    .setTouchMove(model.getDelta())
                    .getResult());
        } else {
            this.mode = Mode.SWIPE;
        }
    }

    private void handleUp(long timestamp) {
        if(timestamp - startClickTime < Config.MAX_CLICK_DURATION) {
            ActionBuilder builder = ActionBuilder.newAction(ActionBuilder.Action.BUTTON);
            builder.setBtnState(ButtonKey.LEFT, true);
            launcher.launch(builder.getResult());
            builder.setBtnState(ButtonKey.LEFT, false);
            launcher.launch(builder.getResult());
        }

        this.mode = Mode.NONE;
    }



    private static class TouchModel {
        double[] start;
        double[] delta;
        private TouchModel() {
            this.start = new double[2];
            this.delta = new double[2];
        }

        public void setStart(float x, float y) {
            this.start[0] = x;
            this.start[1] = y;
        }

        public double moving(float newX, float newY) {
            this.delta[0] = newX - this.start[0];
            this.delta[1] = newY - this.start[1];

            this.start[0] = newX;
            this.start[1] = newY;

            return getDistanceSquare();
        }

        public double getDistanceSquare() {
            return this.delta[0] * this.delta[0] + this.delta[1] *this.delta[1];
        }

        public double[] getDelta() {
            return this.delta;
        }
    }
}

