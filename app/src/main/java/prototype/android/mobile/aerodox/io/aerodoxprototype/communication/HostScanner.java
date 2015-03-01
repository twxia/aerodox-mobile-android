package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import android.os.Handler;

/**
 * Created by maeglin89273 on 3/1/15.
 */
public interface HostScanner {
    public abstract void scan(Handler doneListener);
    public abstract boolean isScanning();
    public abstract void stopScanning();
}
