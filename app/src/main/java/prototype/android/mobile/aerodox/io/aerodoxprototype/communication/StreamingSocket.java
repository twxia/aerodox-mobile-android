package prototype.android.mobile.aerodox.io.aerodoxprototype.communication;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by maeglin89273 on 3/1/15.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public interface StreamingSocket extends Cloneable, AutoCloseable {
    OutputStream getOutputStream() throws IOException;
    InputStream getInputStream() throws IOException;
    boolean isClosed();
    boolean isConnected();
    void close() throws IOException;

}
