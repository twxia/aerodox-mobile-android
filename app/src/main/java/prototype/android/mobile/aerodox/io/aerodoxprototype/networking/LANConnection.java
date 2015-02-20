package prototype.android.mobile.aerodox.io.aerodoxprototype.networking;

import android.os.Handler;
import android.os.Message;

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
    private List<String> availableIP = new ArrayList<>();

    public LANConnection(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        List<String> deviceIPs = getActiveIPs();

        for(String deviceIP : deviceIPs){
            List<String> pingableIPs = checkPingable(deviceIP);
            for(String pingableIP : pingableIPs){
                this.availableIP.add(pingableIP);
            }
        }

        Message msg = new Message();
        msg.what = 1;
        msg.obj = this.availableIP;

        this.mHandler.sendMessage(msg);
    }

    private List<String> checkPingable(String IP){
        ExecutorService es = Executors.newFixedThreadPool(35);
        List<Future<Boolean>> futures = new ArrayList<>();

        List<String> availableIPs = new ArrayList<>();

        List<String> LANIPs = getLAN(IP);
        for (String LANIP : LANIPs){
            futures.add(portIsOpen(es, LANIP, Config.TCP_PORT, Config.TIMEOUT));
        }
        es.shutdown();

        for (final Future<Boolean> f : futures) {
            try {
                if (f.get()) availableIPs.add(LANIPs.get(futures.indexOf(f)));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return availableIPs;
    }

    private List<String> getLAN(String IP){
        List<String> IPs = new ArrayList<>();
        String[] sub = IP.split("\\.");
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

    private Future<Boolean> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(new Callable<Boolean>() {
            @Override public Boolean call() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    socket.close();
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        });
    }

    private List<String> getActiveIPs() {
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
}
