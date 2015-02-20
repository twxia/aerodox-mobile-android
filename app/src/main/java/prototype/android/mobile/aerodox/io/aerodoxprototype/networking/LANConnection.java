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

/**
 * Created by xia on 2/20/15.
 */
public class LANConnection implements Runnable {

    private Handler mHandler;

    public LANConnection(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
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

    private List<String> getLANIPs(String localIP){
        List<String> IPs = new ArrayList<>();
        String[] sub = localIP.split("\\.");
        String host = "";

        for(int i=0; i< sub.length - 1; i++){
            if(i == 0)
                host = sub[i];
            else
                host += "." + sub[i];
        }

        for (int i=1; i<255; i++){
            IPs.add(host + "." + i);
        }

        return IPs;
    }

    private Callable<HostInfo> makeHostChecker(final String ip, final int port) {
        return new Callable<HostInfo>() {
            @Override public HostInfo call() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), Config.TIMEOUT);
                    String host = scanHost(socket);

                    return new HostInfo(host, ip);
                } catch (Exception ex) {
                    return null;
                }
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String scanHost(Socket socket) {
        String host = null;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.write("[");
            writer.write(makeScanJson().toString());
            writer.flush();

            host = handleScanResponse(reader.readLine());
            writer.write("]");
            writer.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return host;
    }

    private static String handleScanResponse(String s) {
        String host = null;
        try {
            JSONObject response = new JSONObject(s);
            if (response.getString("rsp").equals("scan")) {
                host = response.getString("host");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return host;
    }

    private static JSONObject makeScanJson() {
        JSONObject action = new JSONObject();
        try {
            action.put("act", "scan");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return action;
    }

}
