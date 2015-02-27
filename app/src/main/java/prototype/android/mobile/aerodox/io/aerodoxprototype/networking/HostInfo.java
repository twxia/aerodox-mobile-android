package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import java.io.Serializable;

/**
 * Created by maeglin89273 on 2/21/15.
 */
public class HostInfo implements Serializable {


    public enum HostType{LAN, BLUETOOTH};
    private final String hostname;
    private final HostType type;
    private final String address;
    private final String displayString;

    public HostInfo(String hostname, HostType type, String address) {
        this.hostname = hostname;
        this.type = type;
        this.address = address;
        this.displayString = hostname + "\n" + address;
    }

    public String getHostname() {
        return this.hostname;
    }
    public HostType getType() {
        return this.type;
    }
    public String getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        return this.displayString;
    }
}
