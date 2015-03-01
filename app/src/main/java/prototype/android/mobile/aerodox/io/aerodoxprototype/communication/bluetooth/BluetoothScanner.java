package prototype.android.mobile.aerodox.io.aerodoxprototype.communication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostFoundReceiver;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostInfo;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostScanner;

/**
 * Created by maeglin89273 on 3/1/15.
 */
public class BluetoothScanner extends BroadcastReceiver implements HostScanner {
    private HostFoundReceiver receiver;
    private Context register;
    private boolean scanning;
    public BluetoothScanner(Context register, HostFoundReceiver revceiver) {
        this.register = register;
        this.receiver = revceiver;
        this.scanning = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                HostInfo host = new HostInfo(device.getName(), HostInfo.HostType.BLUETOOTH, device.getAddress());
                receiver.hostFound(host);
                break;

            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                this.stopScanning();
        }

    }

    @Override
    public void scan() {
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter finishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        this.register.registerReceiver(this, foundFilter);
        this.register.registerReceiver(this, finishFilter);

        this.scanning = BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    public boolean isScanning() {
        return this.scanning;
    }

    @Override
    public void stopScanning() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        this.register.unregisterReceiver(this);
        this.scanning = false;
    }
}
