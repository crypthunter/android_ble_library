package com.lorenzo.arduinoble_library_test;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
//import della libreria per il bluetooth low energy
import com.lorenzo.ble.BleSearch;
import com.lorenzo.ble.EventConnection;
import com.lorenzo.ble.EventDiscovered;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnScan;
    private ListView lviewDevices;
    private ArrayAdapter mAdapter;
    private ArrayList<String> lviewArray;


    BleSearch bleSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //istanza della libreria bluetooth per la ricerca
        bleSearch = new BleSearch(this);

        //list view che contiene i dispositivi trovati
        lviewDevices = findViewById(R.id.lview_devices);
        //arraylist vuoto usato per inizializzare la lista
        lviewArray = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lviewArray );
        lviewDevices.setAdapter(mAdapter);

    //quando schiaccio un elemento della lista
    lviewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, final View component, int pos, long id){
                final BluetoothDevice device = bleSearch.getDevice(pos);
                if (device == null) return;
                final Intent intent = new Intent(getBaseContext(), DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                if (bleSearch.isScanning()) {
                    bleSearch.stopScan();
                }
                startActivity(intent);
            }
        });

        askGpsPermission();
    }

    @Override
    protected void onPause() {
        bleSearch.stopScan();
        super.onPause();
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
    protected void onDestroy() {
        bleSearch.stopScan();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        btnScan = this.findViewById(R.id.btn_scan);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_scan:
                if(!bleSearch.isScanning())
                {
                    item.setTitle("stop");
                    bleSearch.startScan();
                }
               else
                {
                    item.setTitle("start");
                    bleSearch.stopScan();
                    mAdapter.clear();
                }
                break;
        }
        return true;
    }

    //evento che si verifica quando viene trovato un nuovo dispositivo
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventDiscovered event) {
        lviewArray.add(event.message);
        mAdapter.notifyDataSetChanged();
    }

    //chiede all'utente il permesso di utilizzare la posizione
    public void askGpsPermission()
    {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Questa app necessita dell'accesso alla posizione");
            builder.setMessage("Per favore consenti l'accesso alla posizione per scansionare periferiche BLE");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            });
            builder.show();
        }
    }
}
