package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

/**
 * Created by maeglin89273 on 2/21/15.
 */
public class HostInfo {
    private final String hostname;
    private final String ip;
    private final String displayString;

    public HostInfo(String hostname, String ip) {
        this.hostname = hostname;
        this.ip = ip;
        this.displayString = hostname + "\n" + ip;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getIP() {
        return this.ip;
    }

    @Override
    public String toString() {
        return this.displayString;
    }
}
