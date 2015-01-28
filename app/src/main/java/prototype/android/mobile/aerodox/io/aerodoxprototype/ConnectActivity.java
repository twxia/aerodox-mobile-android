package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
 * Created by xia on 1/18/15.
 */
public class ConnectActivity extends Activity {

    final int port = 1810;

    ListView ipList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        final Button btnConnect = (Button) findViewById(R.id.btnConnect);

        ImageView logo = (ImageView) findViewById(R.id.imgLogo);
        logo.setImageResource(R.drawable.logo);

        ipList = (ListView) findViewById(R.id.ipList);

        final List<String> availableIP = new ArrayList<>();

        ipList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(ConnectActivity.this, MainActivity.class);
                intent.putExtra("ip", availableIP.get(position));
                intent.putExtra("port", port);
                startActivity(intent);
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ConnectActivity.this, MainActivity.class);
                intent.putExtra("ip", getInputIp());
                intent.putExtra("port", port);
                startActivity(intent);

            }
        });

        final Handler myHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(ConnectActivity.this, android.R.layout.simple_list_item_1, availableIP);
                        ipList.setAdapter(listAdapter);
                        break;
                }
                super.handleMessage(msg);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> IPs = getActiveIPs();

                for(String ip : IPs){
                    List<String> LANIPs = checkLAN(ip);
                    for(String LANIP : LANIPs){
                        availableIP.add(LANIP);
                    }
                }
                System.out.println(availableIP.toString());
                Message msg = new Message();
                msg.what = 1;
                myHandler.sendMessage(msg);
            }
        }).start();
    }

    private String getInputIp(){
        final EditText ipSlot1 = (EditText) findViewById(R.id.ipSlot1);
        final EditText ipSlot2 = (EditText) findViewById(R.id.ipSlot2);
        final EditText ipSlot3 = (EditText) findViewById(R.id.ipSlot3);
        final EditText ipSlot4 = (EditText) findViewById(R.id.ipSlot4);

        return ipSlot1.getText().toString() + "." + ipSlot2.getText().toString() + "." +
                ipSlot3.getText().toString() + "." + ipSlot4.getText().toString();
    }

    public List<String> checkLAN(String ip){
        int timeout=350;

        final ExecutorService es = Executors.newFixedThreadPool(20);

        List<String> availableIPs = new ArrayList<>();

        String subnet = "";
        String[] sub = ip.split("\\.");

        for(int i=0; i< sub.length - 1; i++){
            if(i == 0)
                subnet = sub[i];
            else
                subnet += "." + sub[i];
        }

        final List<Future<Boolean>> futures = new ArrayList<>();
        for (int i=1;i<255;i++){
            String host = subnet + "." + i;
            futures.add(portIsOpen(es, host, port, timeout));
        }
        es.shutdown();

        int i = 0;
        for (final Future<Boolean> f : futures) {
            i++;
            try {
                System.out.println(f.get().toString() + ",ip: "+subnet+"."+i);
                if (f.get()) {

                    availableIPs.add(subnet + "." + futures.indexOf(f));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        return availableIPs;
    }

    public Future<Boolean> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(new Callable<Boolean>() {
            @Override public Boolean call() {
                try {
                    Socket socket = new Socket();
                    System.out.println("connect : " + ip);
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
