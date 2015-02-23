package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.HostInfo;
import prototype.android.mobile.aerodox.io.aerodoxprototype.networking.LANScanner;

/**
 * Created by xia on 1/18/15.
 */
public class ConnectActivity extends Activity {

    private ListView hostList;
    private LinearLayout ipLoadingLayout;
    private ArrayAdapter<HostInfo> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        initViews();
        startScanning();
    }
    
    private void initViews() {
//        Button btnConnect = (Button) findViewById(R.id.btnConnect);
        ImageView logo = (ImageView) findViewById(R.id.imgLogo);
        logo.setImageResource(R.drawable.logo);

        ipLoadingLayout = (LinearLayout) findViewById(R.id.ipLoadingLayout);
        initHostList();
    }
    
    private void initHostList() {
        hostList = (ListView) findViewById(R.id.hostList);

        final List<HostInfo> availableHosts = new ArrayList<>();
        listAdapter = new ArrayAdapter<HostInfo>(
                ConnectActivity.this, android.R.layout.simple_list_item_1, availableHosts);

        hostList.setAdapter(listAdapter);

        hostList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(ConnectActivity.this, ControlActivity.class);
                intent.putExtra("ip", availableHosts.get(position).getIP());
                startActivity(intent);
            }
        });
    }

    private void startScanning() {
        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                listAdapter.add((HostInfo)msg.obj);
            }
        };

        final Handler loadingLayoutHider = new Handler() {
            public void handleMessage(Message msg) {
                ipLoadingLayout.setVisibility(View.GONE);
            }
        };

        Thread scanThread = new Thread() {

            @Override
            public void run() {
                new LANScanner(mHandler).scan();
                loadingLayoutHider.sendEmptyMessage(0);
            }
        };

        scanThread.start();
    }

}
