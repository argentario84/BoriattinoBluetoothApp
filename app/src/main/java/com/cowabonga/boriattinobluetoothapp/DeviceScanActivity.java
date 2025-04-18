package com.cowabonga.boriattinobluetoothapp;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class DeviceScanActivity extends ListActivity {
    private static final String TAG = "DeviceScanActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<BluetoothDevice> mDevicesList = new ArrayList<>();
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final long SCAN_PERIOD = 15000; // 10 secondi di scansione
    private Button scanButton;  // Pulsante per avviare lo scan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        setListAdapter(mArrayAdapter);

        // Controlla i permessi
        requestBluetoothPermissions();
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, REQUEST_BLUETOOTH_PERMISSIONS);
            } else {
                initializeBluetooth();
            }
        } else {
            initializeBluetooth();
        }
    }

    private void initializeBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth non supportato");
            Toast.makeText(this, "Bluetooth non supportato", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Controllo se il Bluetooth è attivo
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Dopo che il Bluetooth è stato attivato, ottieni lo scanner BLE
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            Toast.makeText(this, "Scanner BLE non disponibile. Verifica che il Bluetooth sia attivo.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "mBluetoothLeScanner è null");
        } else {
            scanLeDevice(true);
        }
    }


    private void setupScanButton() {
        Log.d("XXX","STO SCANNANDO");
        scanButton = new Button(this);
        scanButton.setText("Scansiona dispositivi");
        scanButton.setOnClickListener(v -> scanLeDevice(true));
        getListView().addHeaderView(scanButton);  // Aggiunge il pulsante sopra la lista
    }


    private void scanLeDevice(final boolean enable) {
        if (mBluetoothLeScanner == null) {
            Log.e(TAG, "BLE Scanner non inizializzato");
            return;
        }

        if (enable) {
            Log.d(TAG, "Inizio scansione BLE...");

            mHandler.postDelayed(() -> {
                mScanning = false;
                Log.d(TAG, "Scansione BLE terminata.");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    mBluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mDevicesList.clear();
            mArrayAdapter.clear();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                mBluetoothLeScanner.startScan(leScanCallback);
            }
        } else {
            mScanning = false;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                mBluetoothLeScanner.stopScan(leScanCallback);
            }
        }
    }


    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("AAA","AAA");
            BluetoothDevice device = result.getDevice();
            if (!mDevicesList.contains(device) && device.getName() != null) {
                mDevicesList.add(device);
                runOnUiThread(() -> {
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mArrayAdapter.notifyDataSetChanged();
                });
            }
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Se viene premuto il pulsante, ignora il click
        if (position == 0) return;

        BluetoothDevice device = mDevicesList.get(position - 1);  // Compensa per il pulsante
        Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra("DEVICE_ADDRESS", device.getAddress());
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeBluetooth();
            } else {
                Toast.makeText(this, "Permessi Bluetooth negati", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
