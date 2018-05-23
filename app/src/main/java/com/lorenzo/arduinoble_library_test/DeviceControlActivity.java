package com.lorenzo.arduinoble_library_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.lorenzo.ble.BleConnect;

public class DeviceControlActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String deviceName;
    private String deviceAddress;
    private Button btnRead;
    private Button btnWrite;
    private BleConnect bc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        //setup UI
        btnRead = this.findViewById(R.id.btn_rx);
        btnWrite = this.findViewById(R.id.btn_tx);

        //recupero nome e indirizzo del dispositivo selezionato dalla main activity
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        getSupportActionBar().setTitle(deviceName);

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
}
