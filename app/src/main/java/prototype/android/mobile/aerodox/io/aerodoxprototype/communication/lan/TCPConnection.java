package prototype.android.mobile.aerodox.io.aerodoxprototype.communication.lan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.StreamingConnection;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.StreamingSocket;

/**
 * Created by maeglin89273 on 2/25/15.
 */
public class TCPConnection extends StreamingConnection {

    public TCPConnection(String address) {
        super(address);
    }

    @Override
    protected StreamingSocket connectAsStreamingSocket() throws IOException {
        Socket tcpSocket = new Socket();
        //add socket configuration here
        tcpSocket.setSendBufferSize(120);

        tcpSocket.connect(new InetSocketAddress(this.address, Config.TCP_PORT), Config.TIMEOUT);

        return new StreamingSocketAdapter(tcpSocket);
    }


    private static class StreamingSocketAdapter implements StreamingSocket {
        private Socket socket;

        public StreamingSocketAdapter(Socket socket) {
            this.socket = socket;
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
            return this.socket.isClosed();
        }

        @Override
        public boolean isConnected() {
            return this.socket.isConnected();
        }

        @Override
        public void close() throws IOException {
            this.socket.close();
        }
    }
}
