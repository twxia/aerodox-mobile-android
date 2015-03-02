package prototype.android.mobile.aerodox.io.aerodoxprototype.controlling;

import android.view.MotionEvent;
import android.view.View;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.Connection;

/**
* Created by maeglin89273 on 2/23/15.
*/
public class TouchMediator implements View.OnTouchListener {
    private static final String[][] ARROWS = new String[][] {{"LEFT", "RIGHT"}, {"UP", "DOWN"}};

    private GyroSignalEmitter gyroEmt;
    private Connection actionLauncher;
    private final TouchModel model;


    public TouchMediator(GyroSignalEmitter gyroEmt, Connection actionLauncher) {
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
                break;

            default:
                return false;
        }

        return true;
    }

    private void handleDown(float x, float y) {
        model.setStart(x, y);
        gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.SWIPE);
    }

    private void handleMove(float x, float y) {
        double distanceSquare = model.moving(x, y);
        if (gyroEmt.isLocked()) {
            touchMove();
        } else { //unlocked
            if (distanceSquare < Config.MOVE_THRESHOLD) {
                gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.SWIPE);
            }
        }
    }

    private void handleUp(long downDuration) {

        if(TouchModel.getSquare(model.getSpeed()) < Config.MOVE_THRESHOLD) {
           if(downDuration < Config.MAX_CLICK_DURATION) {
               singleClick();
           }
        } else if (!gyroEmt.isLocked()) {
            arrowKey();

        }

        gyroEmt.setEmmittingMode(GyroSignalEmitter.EmittingMode.MOVE);
    }

    private void arrowKey() {
        double[] speed = model.getSpeed();
        int dirIndex = signIndexMapping(Math.signum(Math.abs(speed[1]) - Math.abs(speed[0])));
        String dir = ARROWS[dirIndex][signIndexMapping(Math.signum(speed[dirIndex]))];

        actionLauncher.launchAction(ActionBuilder.newAction(Header.ARROW).setDirection(dir).getResult());

    }

    private static int signIndexMapping(double sign) {
        return ((int)sign + 1) / 2;
    }

    private void singleClick() {
        ActionBuilder builder = ActionBuilder.newAction(Header.BUTTON);
        builder.setBtnState(ButtonKey.LEFT, true);
        actionLauncher.launchAction(builder.getResult());
        builder.setBtnState(ButtonKey.LEFT, false);
        actionLauncher.launchAction(builder.getResult());
    }

    private void touchMove() {
        actionLauncher.launchAction(ActionBuilder.newAction(Header.TOUCH)
                .setTouchMove(model.getSpeed())
                .getResult());
    }


    private static class TouchModel {
        double[] lastPoint;
        double[] distance;
        private double[] speed;

        private TouchModel() {
            this.lastPoint = new double[2];
            this.distance = new double[2];
            this.speed = new double[2];
        }

        public void setStart(float x, float y) {
            this.lastPoint[0] = x;
            this.lastPoint[1] = y;
            this.distance[0] = this.distance[1] = 0;
            this.speed[0] = this.speed[1] = 0;
        }

        public double moving(float newX, float newY) {

            speed[0] = newX - this.lastPoint[0];
            speed[1] = newY - this.lastPoint[1];

            this.lastPoint[0] = newX;
            this.lastPoint[1] = newY;
            this.distance[0] += speed[0];
            this.distance[1] += speed[1];

            return getSquare(speed);
        }

        public static double getSquare(double[] vec) {
            return vec[0] * vec[0] + vec[1] * vec[1];
        }

        public double[] getSpeed() {
            return speed;
        }

        public double[] getDistance() {
            return this.distance;
        }
    }
}

