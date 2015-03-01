package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import android.os.Handler;
import android.os.Message;

/**
 * Created by maeglin89273 on 3/1/15.
 */
public class HostFoundReceiver {
    private Handler delegate;
    public HostFoundReceiver(Handler delegate) {
        this.delegate = delegate;
    }

    public void hostFound(HostInfo host) {
        Message msg = new Message();
        msg.obj = host;

        this.delegate.sendMessage(msg);
    }
}
