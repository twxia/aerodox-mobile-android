package prototype.android.mobile.aerodox.io.aerodoxprototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by xia on 1/18/15.
 */
public class ConnectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        final Button btnConnect = (Button) findViewById(R.id.btnConnect);

        ImageView logo = (ImageView) findViewById(R.id.imgLogo);
        logo.setImageResource(R.drawable.logo);


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ConnectActivity.this, MainActivity.class);
                intent.putExtra("ip", getInputIp());
                startActivity(intent);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        connectSocket(getInputIp(), port);
//
//                        while(true){
//
//                        }
//                    }
//                }).start();

            }
        });
    }

    private String getInputIp(){
        final EditText ipSlot1 = (EditText) findViewById(R.id.ipSlot1);
        final EditText ipSlot2 = (EditText) findViewById(R.id.ipSlot2);
        final EditText ipSlot3 = (EditText) findViewById(R.id.ipSlot3);
        final EditText ipSlot4 = (EditText) findViewById(R.id.ipSlot4);

        return ipSlot1.getText().toString() + "." + ipSlot2.getText().toString() + "." +
                ipSlot3.getText().toString() + "." + ipSlot4.getText().toString();
    }

}
