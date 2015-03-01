package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.bluetooth.BluetoothConnection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.lan.LANConnection;

/**
 * Created by maeglin89273 on 2/27/15.
 */
public abstract class ConnectionFactory {
    public static Connection newConnection(HostInfo args) {
        String address = args.getAddress();
        switch (args.getMode()) {
            case LAN:
                return new LANConnection(address);
            case BLUETOOTH:
                return new BluetoothConnection(address);
        }

        return null;
    }
}
