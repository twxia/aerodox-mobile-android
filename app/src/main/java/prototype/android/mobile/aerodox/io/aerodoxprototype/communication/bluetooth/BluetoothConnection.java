package prototype.android.mobile.aerodox.io.aerodoxprototype.communication.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.StreamingConnection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.StreamingSocket;

/**
 * Created by maeglin89273 on 3/1/15.
 */
public class BluetoothConnection extends StreamingConnection {

    public BluetoothConnection(String address) {
        super(address);
    }

    @Override
    protected StreamingSocket connectAsStreamingSocket() throws IOException {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.address);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(Config.uuid);
        socket.connect();
        return new StreamingSocketAdapter(socket);
    }

    private static class StreamingSocketAdapter implements StreamingSocket {
        private BluetoothSocket socket;
        private boolean closed;
        public StreamingSocketAdapter(BluetoothSocket socket) {
            this.socket = socket;
            this.closed = false;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.socket.getOutputStream();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.socket.getInputStream();
        }

        @Override
        public boolean isClosed() {
            return this.closed;
        }

        @Override
        public boolean isConnected() {
            return this.socket.isConnected();
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
            this.socket.close();
        }
    }
}
