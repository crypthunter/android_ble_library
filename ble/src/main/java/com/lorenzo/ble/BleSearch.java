package com.lorenzo.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BleSearch {

    //contiene i dispositivi bluetooth trovati
    private ArrayList<BluetoothDevice> leDeviceList;
    //serve per creare BluetoothAdapter
    final BluetoothManager bluetoothManager;
    //ha i metodi startLeScan e stopLeScan
    private BluetoothAdapter mBluetoothAdapter;
    //per verificare se sto cercando
    private boolean mScanning;
    //serve per far partire un thread dopo un certo intervallo di tempo, nel caso sia attivo lo stop automatico
    private Handler mHandler;
    //contesto dell'activity in cui viene usata la libreria
    Context mContext;

    public BleSearch(Context mContext)
    {
        this.mContext = mContext;
        mHandler = new Handler();
        bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        leDeviceList = new ArrayList<>();
        mScanning = false;
    }

    //ritorna true se il bluetooth è supportato
    public boolean isBluetoothSupported()
    {
        boolean supported = true;
        if (mBluetoothAdapter == null)
            supported = false;
        return supported;
    }

    //ritorna true se il bluetooth low energy è supportato
    public boolean isBleSupported()
    {
        boolean supported = true;
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            supported = false;
        return supported;
    }

    //ritorna true se sta scansionando
    public boolean isScanning()
    {
        return mScanning;
    }


    //fa partire o ferma la scansione
    /*public void scanLeDevice(final boolean enable, long scanPeriod) {
        if (enable) {
            leDeviceList.clear();
            //ferma la scansione dopo un intervallo di tempo scelto
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }*/

    //fa partire la scansione
    public void startScan()
    {
        leDeviceList.clear();
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    //ferma la scansione
    public void stopScan()
    {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    //quando trova un dispositivo
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addDevice(device);
                        }
                    });
                }
            };

    //aggiunge un dispositivo all'array list se non esisteva già, ignora i dispositivi sconosciuti
    public void addDevice(BluetoothDevice device) {
        if (device.getName() == null)
            Log.d("BleSearch", "dispositivo sconosciuto ignorato");
        else if(!leDeviceList.contains(device)) {
            leDeviceList.add(device);
            Log.d("BleSearch",device.getName());
        }
    }

    //ritorna il dispositivo in posizione pos
    public BluetoothDevice getDevice(int pos)
    {
        return leDeviceList.get(pos);
    }

    //ritorna il numero di dispositivi trovati
    public int devicesNumber()
    {
        return leDeviceList.size();
    }

}
