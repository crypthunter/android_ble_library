package com.lorenzo.ble;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static android.content.Context.BIND_AUTO_CREATE;

public class BleConnect {
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected;
    private BluetoothLeService mBluetoothLeService;
    private Context mContext;
    //caratteristica per trasmettere dati al sensore
    private BluetoothGattCharacteristic characteristicTX;
    //caratteristica per ricevere dati dal sensore
    private BluetoothGattCharacteristic characteristicRX;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    //dato ricevuto dal sensore
    private String data;

    //Ciclo di vita del servizio
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.d("BleConnect", "impossibile inizializzare il bluetooth");
                ((Activity)mContext).finish();
            }
            connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Gestisce vari eventi attivati dal servizio BLE
    // ACTION_GATT_CONNECTED: connesso ad un server GATT
    // ACTION_GATT_DISCONNECTED: disconnesso da un server GATT
    // ACTION_GATT_SERVICES_DISCOVERED: scoperti servixi GATT
    // ACTION_DATA_AVAILABLE: ricevuto un dato: è il risultato di una notifica o di una lettura
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.d("BleConnect", "connesso");
                EventBus.getDefault().post(new EventConnection("connesso"));
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d("BleConnect", "disconnesso");
                EventBus.getDefault().post(new EventConnection("disconnesso"));
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.d("BleConnect", "servizio scoperto");
                //abilita notifiche per ricevere il dato ogni volta che arduino invia (anche se è uguale)
                enableNotification(true);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                data = intent.getStringExtra(mBluetoothLeService.EXTRA_DATA);
                Log.d("BleConnect", "dato letto = " + data);
                EventBus.getDefault().post(new EventData(data));
            }
        }
    };

    public BleConnect(String mDeviceName, String mDeviceAddress, Context mContext)
    {
        this.mDeviceName = mDeviceName;
        this.mDeviceAddress = mDeviceAddress;
        this.mContext = mContext;
        mConnected = false;
        Intent gattServiceIntent = new Intent(((Activity)mContext), BluetoothLeService.class);
        mContext.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    //per connettersi ad un dispositivo
    public void connect()
    {
        boolean b = mBluetoothLeService.connect(mDeviceAddress);
        mConnected = b;
        Log.d("BleConnect", "connesso = " + b);
    }

    public void disconnect()
    {
        mBluetoothLeService.disconnect();
    }

    //serve per far funzionare BroadCast receiver
    //va chiamato in onResume() dell'activity
    public void registerReceiver()
    {
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    //va chiamato in onPause() dell'activity
    public void unregisterReceiver()
    {
        mContext.unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Servizio sconosciuto";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        //ciclo su tutti i servizi disponibili
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            //get delle caratteristiche per leggere e scrivere quando corrispondono ai rispettivi UUID, andrebbe fatto un if per i diversi dispositivi
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HC_08_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HC_08_RX_TX);
        }
    }

    //legge un dato dal bluetooth
    public void read()
    {
        mBluetoothLeService.readCharacteristic(characteristicRX);
    }

    //scrive un dato sul bluetooth
    public void write(String value)
    {
        characteristicRX.setValue(value);
        mBluetoothLeService.writeCharacteristic(characteristicTX);
    }

    //abilita o disabilita le notifiche
    //quando abilitate, ogni volta che il dato cambia viene ricevuto dal dispositivo android
    public void enableNotification(boolean b)
    {
        mBluetoothLeService.setCharacteristicNotification(characteristicRX, b);
    }

}
