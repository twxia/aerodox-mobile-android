package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import java.io.Serializable;

/**
 * Created by maeglin89273 on 2/21/15.
 */
public class HostInfo implements Serializable {

    private final String hostname;
    private final Config.Mode mode;
    private final String address;
    private final String displayString;

    public HostInfo(String hostname, Config.Mode mode, String address) {
        this.hostname = hostname;
        this.mode = mode;
        this.address = address;
        this.displayString = hostname + "\n" + address;
    }

    public String getHostname() {
        return this.hostname;
    }
    public Config.Mode getMode() {
        return this.mode;
    }
    public String getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        return this.displayString;
    }
}
