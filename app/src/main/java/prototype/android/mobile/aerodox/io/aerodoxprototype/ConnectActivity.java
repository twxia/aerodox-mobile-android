package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.Config;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostFoundReceiver;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostInfo;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.HostScanner;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.bluetooth.BluetoothScanner;
import prototype.android.mobile.aerodox.io.aerodoxprototype.communication.lan.LANScanner;

/**
 * Created by xia on 1/18/15.
 */
public class ConnectActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;

    private ListView hostList;
    private LinearLayout ipLoadingLayout;
    private ArrayAdapter<HostInfo> listAdapter;
    private HostScanner scanner;
    private HostFoundReceiver receiver;
    private Handler loadingLayoutHider;
    private Switch modeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        initCallbacks();
        initViews();

        switchTo(Config.Mode.LAN);
    }

    private void initCallbacks() {
        this.receiver = new HostFoundReceiver(new Handler() {
            public void handleMessage(Message msg) {
                HostInfo info = (HostInfo) msg.obj;
                if(listAdapter.getPosition(info) < 0) { // not found before
                    listAdapter.add(info);
                }
            }
        });

        this.loadingLayoutHider = new Handler() {
            public void handleMessage(Message msg) {
                ipLoadingLayout.setVisibility(View.GONE);

            }
        };
    }

    private void initViews() {

        ImageView logo = (ImageView) findViewById(R.id.imgLogo);
        logo.setImageResource(R.drawable.logo);

        ipLoadingLayout = (LinearLayout) findViewById(R.id.ipLoadingLayout);
        initModeSwitch();
        initHostList();
        initRefreshButton();

    }

    private void initModeSwitch() {
        modeSwitch = (Switch) findViewById(R.id.connModeSwitch);
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    askTurnOnBluetooth();
                } else {
                    switchTo(Config.Mode.LAN);
                }
            }
        });
    }

    private void askTurnOnBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support bluetooth", Toast.LENGTH_SHORT);
            modeSwitch.setChecked(false);
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        switchTo(Config.Mode.BLUETOOTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                switchTo(Config.Mode.BLUETOOTH);
            } else {
                modeSwitch.setChecked(false);
            }
        }
    }

    private void initRefreshButton() {
        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
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
                scanner.stopScanning();
                Intent intent = new Intent();
                intent.setClass(ConnectActivity.this, ControlActivity.class);
                intent.putExtra("host", availableHosts.get(position));
                startActivity(intent);
            }
        });
    }


    private void switchTo(Config.Mode mode) {
        if (scanner != null) {
            scanner.stopScanning();
        }

        switch (mode) {
            case LAN:
                this.scanner = new LANScanner(receiver);
                break;
            case BLUETOOTH:
                this.scanner = new BluetoothScanner(ConnectActivity.this, receiver);
                break;
            default:
                return;
        }

        startScan();
    }

    private void startScan() {
        scanner.stopScanning();
        listAdapter.clear();

        scanner.scan(loadingLayoutHider);
        ipLoadingLayout.setVisibility(View.VISIBLE);
    }
}
