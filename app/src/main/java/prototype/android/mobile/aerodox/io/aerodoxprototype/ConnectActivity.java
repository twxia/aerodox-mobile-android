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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
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

import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.LANConnection;

/**
 * Created by xia on 1/18/15.
 */
public class ConnectActivity extends Activity {

    ListView ipList;
    LinearLayout ipLoadingLauout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        final Button btnConnect = (Button) findViewById(R.id.btnConnect);

        ImageView logo = (ImageView) findViewById(R.id.imgLogo);
        logo.setImageResource(R.drawable.logo);

        ipList = (ListView) findViewById(R.id.ipList);
        ipLoadingLauout = (LinearLayout) findViewById(R.id.ipLoadingLauout);

        final List<String> availableIP = new ArrayList<>();

        ipList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(ConnectActivity.this, MainActivity.class);
                intent.putExtra("ip", availableIP.get(position));
                startActivity(intent);
            }
        });

        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                                ConnectActivity.this, android.R.layout.simple_list_item_1, availableIP);

                        ipLoadingLauout.setVisibility(View.INVISIBLE);
                        ipList.setAdapter(listAdapter);
                        break;
                }
                super.handleMessage(msg);
            }
        };

        new Thread(new LANConnection(mHandler)).start();
    }

}
