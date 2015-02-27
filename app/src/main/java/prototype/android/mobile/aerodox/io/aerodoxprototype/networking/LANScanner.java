package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.ActionBuilder;
import prototype.android.mobile.aerodox.io.aerodoxprototype.controling.Header;

/**
 * Created by xia on 2/20/15.
 */
public class LANScanner {

    private Handler mHandler;

    public LANScanner(Handler hostMessageReciever) {
        this.mHandler = hostMessageReciever;
    }


    public void scan() {
        List<String> deviceIPs = getActiveIPs();

        for(String deviceIP : deviceIPs){
            scanLAN(deviceIP);
        }
    }

    private void scanLAN(String IP){
        ExecutorService executor = Executors.newFixedThreadPool(35);
        List<Future<HostInfo>> futures = new ArrayList<>();
        Future<HostInfo> future;

        List<String> LANIPs = getLANIPs(IP);
        for (String LANIP : LANIPs){
            future = executor.submit(makeHostChecker(LANIP, Config.TCP_PORT));
            futures.add(future);
        }

        executor.shutdown();

        for (Future<HostInfo> f : futures) {
            try {
                HostInfo hostInfo = f.get();
                if (hostInfo != null)
                    sendAvaliableHostMessage(hostInfo);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //send the scanning result immediately, leveraging concurrency
    private void sendAvaliableHostMessage(HostInfo hostInfo) {
        Message msg = new Message();
        msg.obj = hostInfo;

        this.mHandler.sendMessage(msg);
    }

    private static List<String> getLANIPs(String localIP){
        List<String> IPs = new ArrayList<>();
        String host = localIP.substring(0, localIP.lastIndexOf('.') + 1);

        for (int i=1; i<255; i++){
            IPs.add(host + i);
        }

        return IPs;
    }

    private static Callable<HostInfo> makeHostChecker(final String ip, final int port) {
        return new Callable<HostInfo>() {
            @Override public HostInfo call() {

                    String host = scanHost(ip, port);
                    if (host != null) {
                        return new HostInfo(host, HostInfo.HostType.LAN, ip);
                    }
                    return null;
            }
        };
    }

    private static List<String> getActiveIPs() {
        List<String> result = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface netInterface;
            for (; netInterfaces.hasMoreElements();) {

                netInterface = netInterfaces.nextElement();
                if (!netInterface.isLoopback() && !netInterface.isVirtual() && netInterface.isUp()) {

                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    InetAddress address;
                    for (; addresses.hasMoreElements();) {

                        address = addresses.nextElement();
                        if (address instanceof Inet4Address) {
                            result.add(address.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return result;
    }


    private static String scanHost(String ip, int port) {
        SyncTCPConnection connection = new SyncTCPConnection(ip, port);
        String host = null;
        try {
            JSONObject response = connection.post(ActionBuilder.newAction(Header.SCAN).getResult());
            host = response.getString("host");
        } catch (IOException| JSONException e) {
            //timeout
        }

        return host;
    }



    private static class SyncTCPConnection {
        private final InetSocketAddress clientIP;

        private SyncTCPConnection(String ip, int port) {
            clientIP = new InetSocketAddress(ip, port);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public JSONObject post(JSONObject request) throws IOException {
            Socket socket = new Socket();
            socket.connect(clientIP, Config.TIMEOUT);

            JSONObject response = null;
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                writer.write("[");
                writer.write(request.toString());
                writer.flush();

                response = new JSONObject(reader.readLine());
                writer.write("]");
                writer.flush();
                socket.close();
                return response;
            } catch (JSONException| IOException e) {
                e.printStackTrace();

            }

            throw new IOException("connection is not stable");
        }
    }
}
