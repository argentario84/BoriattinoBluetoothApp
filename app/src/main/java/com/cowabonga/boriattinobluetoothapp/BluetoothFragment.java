package com.cowabonga.boriattinobluetoothapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private DeviceListAdapter deviceListAdapter;
    private ArrayList<BluetoothDevice> deviceList;
    private ListView deviceListView;
    private Handler handler = new Handler();

    private boolean isScanning = false;

    public BluetoothFragment() {
        // Costruttore vuoto obbligatorio per i Fragments
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        deviceListView = view.findViewById(R.id.device_list);
        Button scanButton = view.findViewById(R.id.scan_button);

        deviceList = new ArrayList<>();
        deviceListAdapter = new DeviceListAdapter(getActivity(), R.layout.device_list_item, deviceList);
        deviceListView.setAdapter(deviceListAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth non supportato", Toast.LENGTH_LONG).show();
            return view;
        }

        // Controlla se il Bluetooth è attivo
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Mostra dispositivi già accoppiati
        showPairedDevices();

        // Inizia scansione quando il pulsante viene premuto
        scanButton.setOnClickListener(v -> {
            Log.d("DEBUG_BLE", "Pulsante Scan premuto!");
            if (!isScanning) {
                startScan();
            } else {
                stopScan();
            }
        });

        // Click su un dispositivo per connettersi
        deviceListView.setOnItemClickListener((AdapterView<?> parent, View view1, int position, long id) -> {
            BluetoothDevice device = deviceList.get(position);
            Toast.makeText(getActivity(), "Connetto a " + device.getName(), Toast.LENGTH_SHORT).show();

            // Apri la schermata di connessione (da implementare)
            Intent intent = new Intent(getActivity(), DeviceControlActivity.class);
            intent.putExtra("DEVICE_NAME", device.getName());
            intent.putExtra("DEVICE_ADDRESS", device.getAddress());
            startActivity(intent);
        });

        return view;
    }

    @SuppressLint("MissingPermission")
    private void showPairedDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList.clear();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
            }
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    private void startScan() {
        Intent intent = new Intent(getActivity(), DeviceScanActivity.class);
        startActivity(intent);
    }

    private void stopScan() {
        if (bluetoothAdapter == null) return;

        Toast.makeText(getActivity(), "Scansione terminata", Toast.LENGTH_SHORT).show();
        isScanning = false;
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
