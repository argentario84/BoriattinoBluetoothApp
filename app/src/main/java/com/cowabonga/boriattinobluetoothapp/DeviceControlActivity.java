package com.cowabonga.boriattinobluetoothapp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.UUID;

public class DeviceControlActivity extends AppCompatActivity {
    private static final String TAG = "DeviceControlActivity";
    private BluetoothGatt mBluetoothGatt;
    private TextView mDataField;
    private BluetoothGattCharacteristic targetCharacteristic;
    private Handler handler = new Handler();

    // 🔹 **INDIRIZZO MAC FISSO DEL DISPOSITIVO BLE**
    private static String DEVICE_ADDRESS = "30:C9:22:EF:9B:3A";

    // 🔹 **UUID Servizio e Caratteristica**
    private static final UUID SERVICE_UUID = UUID.fromString("16c7d0ff-80e8-4258-bf48-85c85a71f4e9");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("343b5821-2507-4c39-9312-4108aead25fe");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        mDataField = findViewById(R.id.data_value);

        Log.d(TAG, "Device Address: " + DEVICE_ADDRESS);
        DEVICE_ADDRESS = getIntent().getStringExtra("DEVICE_ADDRESS");

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth non supportato");
            finish();
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mBluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to device. Discovering services...");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from device.");
                mBluetoothGatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered.");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    targetCharacteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (targetCharacteristic != null) {
                        Log.d(TAG, "Characteristic found. Enabling notifications...");
                        enableNotifications(gatt, targetCharacteristic);

                        // **Avvia la lettura forzata ogni 0.25 secondi**
                        handler.postDelayed(readRunnable, 100);
                    } else {
                        Log.e(TAG, "Characteristic not found!");
                    }
                } else {
                    Log.e(TAG, "Service not found!");
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                String receivedData = new String(characteristic.getValue());
                Log.d(TAG, "Received data: " + receivedData);
                updateUI(receivedData);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                String receivedData = new String(characteristic.getValue());
                Log.d(TAG, "Read data: " + receivedData);
                updateUI(receivedData);
            }
        }
    };

    private void enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    private void updateUI(String data) {
        runOnUiThread(() -> mDataField.setText(data));
    }

    private final Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothGatt != null && targetCharacteristic != null) {
                Log.d(TAG, "Forcing read...");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothGatt.readCharacteristic(targetCharacteristic);
                handler.postDelayed(this, 250);  // Leggi ogni 0.25 secondi
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        handler.removeCallbacks(readRunnable);
    }
}
