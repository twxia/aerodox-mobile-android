package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

/**
 * Created by maeglin89273 on 2/27/15.
 */
public abstract class ConnectionFactory {
    public static Connection newConnection(HostInfo args) {
        switch (args.getType()) {
            case LAN:
                return new LANConnection(args.getAddress());
            case BLUETOOTH:
        }

        return null;
    }
}
