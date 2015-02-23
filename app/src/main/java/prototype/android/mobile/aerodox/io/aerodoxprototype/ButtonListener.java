package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.view.MotionEvent;
import android.view.View;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ButtonKey;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.UDPConnection;

/**
* Created by maeglin89273 on 2/23/15.
*/
class ButtonListener implements View.OnTouchListener {

    private UDPConnection actionLauncher;
    ButtonListener(UDPConnection actionLauncher) {
        this.actionLauncher = actionLauncher;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ButtonKey btnKey = (v.getId() == R.id.btnLeft) ? ButtonKey.LEFT : ButtonKey.RIGHT;
        boolean btnPress = false;

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                btnPress = true;
                break;
            case MotionEvent.ACTION_UP:
                btnPress = false;
                break;
        }

        actionLauncher.launch(ActionBuilder.newAction(ActionBuilder.Action.BUTTON)
                .setBtnState(btnKey, btnPress)
                .getResult());
        return false;
    }
}
