package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.view.MotionEvent;
import android.view.View;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ButtonKey;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Header;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.Connection;

/**
* Created by maeglin89273 on 2/23/15.
*/
class TouchMediator implements View.OnTouchListener {


    private GyroSignalEmitter gyroEmt;
    private Connection actionLauncher;
    private final TouchModel model;


    TouchMediator(GyroSignalEmitter gyroEmt, Connection actionLauncher) {
        this.gyroEmt = gyroEmt;
        this.actionLauncher = actionLauncher;
        this.model = new TouchModel();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDown(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                handleMove(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
               handleUp(event.getEventTime() - event.getDownTime());

        }

        return true;
    }

    private void handleDown(float x, float y) {
        model.setStart(x, y);
        gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.SWIPE);
    }

    private void handleMove(float x, float y) {

        double distanceSquare = model.moving(x, y);
        if(distanceSquare > Config.MOVE_THRESHOLD) {
            gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.STOP);
            actionLauncher.launchAction(ActionBuilder.newAction(Header.TOUCH)
                    .setTouchMove(model.getDelta())
                    .getResult());
        } else {
            gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.SWIPE);
        }
    }

    private void handleUp(long downDuration) {
        if(downDuration < Config.MAX_CLICK_DURATION) {
            ActionBuilder builder = ActionBuilder.newAction(Header.BUTTON);
            builder.setBtnState(ButtonKey.LEFT, true);
            actionLauncher.launchAction(builder.getResult());
            builder.setBtnState(ButtonKey.LEFT, false);
            actionLauncher.launchAction(builder.getResult());
        }

        gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.MOVE);
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

