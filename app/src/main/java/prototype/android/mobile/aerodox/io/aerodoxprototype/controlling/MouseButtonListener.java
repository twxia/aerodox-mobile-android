package prototype.android.mobile.aerodox.io.aerodoxprototype.controlling;

import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import prototype.android.mobile.aerodox.io.aerodoxprototype.R;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.Connection;

/**
* Created by maeglin89273 on 2/23/15.
*/
public class MouseButtonListener implements View.OnTouchListener {

    private GyroSignalEmitter gyroEmt;
    private Connection actionLauncher;
    private Map<ButtonKey, Long> lastDownTime;

    public MouseButtonListener(GyroSignalEmitter gyroEmt, Connection actionLauncher) {
        this.gyroEmt = gyroEmt;
        this.actionLauncher = actionLauncher;

        lastDownTime = new HashMap<ButtonKey, Long>();
        this.resetLastDownTime();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ButtonKey btnKey = (v.getId() == R.id.btnLeft) ? ButtonKey.LEFT : ButtonKey.RIGHT;

        boolean btnPress, isLockAction = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                btnPress = true;
                isLockAction = checkGyroLock(btnKey, event.getEventTime());

                break;
            case MotionEvent.ACTION_UP:
                btnPress = false;

                break;
            default:
                return false;
        }

        if (!isLockAction) {
            actionLauncher.launchAction(ActionBuilder.newAction(Header.BUTTON)
                    .setBtnState(btnKey, btnPress)
                    .getResult());
        }
        return false;
    }

    private boolean checkGyroLock(ButtonKey btnKey, long eventTime) {
        lastDownTime.put(btnKey, eventTime);
        long duration = Math.abs(lastDownTime.get(ButtonKey.LEFT).longValue() - lastDownTime.get(ButtonKey.RIGHT).longValue());

        if (duration <= Config.MAX_LOCK_DURATION) {
            if (this.gyroEmt.isLocked()) {
                gyroEmt.unlock();
            } else {
                gyroEmt.lock();
            }
            this.resetLastDownTime();
            return true;
        }

        return false;
    }

    private void resetLastDownTime() {
        lastDownTime.put(ButtonKey.LEFT, 0L);
        lastDownTime.put(ButtonKey.RIGHT, 0L);
    }
}
