package com.cowabonga.boriattinobluetoothapp;

import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataRecordingActivity extends AppCompatActivity {
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_recording);
        statusView = findViewById(R.id.status);

        saveDataToFile("Example data from BLE...");
    }

    private void saveDataToFile(String data) {
        File file = new File(Environment.getExternalStorageDirectory(), "BLEData.txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append(data).append("\n");
            statusView.setText("Dati salvati");
        } catch (IOException e) {
            statusView.setText("Errore nel salvataggio");
        }
    }
}
