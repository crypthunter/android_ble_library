package com.lorenzo.arduinoble_library_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.EventLog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lorenzo.ble.BleConnect;
import com.lorenzo.ble.EventConnection;
import com.lorenzo.ble.EventData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DeviceControlActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String deviceName;
    private String deviceAddress;
    private Button btnRead;
    private Button btnWrite;
    private TextView txtData;
    private TextView txtConnect;
    private TextView txtAddress;
    private BleConnect bc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        //setup UI
        btnRead = this.findViewById(R.id.btn_rx);
        btnWrite = this.findViewById(R.id.btn_tx);
        txtData = this.findViewById(R.id.txt_data);
        txtConnect = this.findViewById(R.id.txt_state);
        txtAddress = this.findViewById(R.id.txt_address);

        //recupero nome e indirizzo del dispositivo selezionato dalla main activity
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        getSupportActionBar().setTitle(deviceName);
        txtAddress.setText(deviceAddress);

        bc = new BleConnect(deviceName, deviceAddress, this);

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bc.read();
            }
        });

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bc.write("1");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        bc.registerReceiver();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bc.unregisterReceiver();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        bc.disconnect();
    }

    //evento per ricevere i dati dal bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData event) {
        txtData.setText(event.message);
    }

    //evento per ricevere l'avvenuta connessione / disconnessione del dispositivo
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventConnection event) {
        txtConnect.setText(event.message);
    }

}
