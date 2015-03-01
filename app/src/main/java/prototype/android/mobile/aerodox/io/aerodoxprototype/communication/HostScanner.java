package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

/**
 * Created by maeglin89273 on 3/1/15.
 */
public interface HostScanner {
    public abstract void scan();
    public abstract boolean isScanning();
    public abstract void stopScanning();
}
