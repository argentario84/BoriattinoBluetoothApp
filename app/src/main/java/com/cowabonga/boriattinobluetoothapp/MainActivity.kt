package com.cowabonga.boriattinobluetoothapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cowabonga.boriattinobluetoothapp.fragments.GpsModeFragment
import com.cowabonga.boriattinobluetoothapp.fragments.LiveModeFragment
import com.cowabonga.boriattinobluetoothapp.fragments.RaceModeFragment
import com.cowabonga.boriattinobluetoothapp.fragments.RouteModeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
   private val REQUEST_CODE_BLUETOOTH = 1001 // Codice per la richiesta di permessi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, GpsModeFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            var selected: Fragment? = null
            if (item.itemId == R.id.navigation_gps) {
                selected = GpsModeFragment()
            } else if (item.itemId == R.id.navigation_live) {
                selected = LiveModeFragment()
            } else if (item.itemId == R.id.navigation_race) {
                selected = RaceModeFragment()
            }
            else if (item.itemId == R.id.navigation_route) {
                selected = RouteModeFragment()
            }
            if (selected != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, selected)
                    .commit()
                return@setOnItemSelectedListener true
            }
            false
        }

        checkBluetoothPermissions()
        // Carica il BluetoothFragment all'avvio
        if (savedInstanceState == null) {
            loadFragment(BluetoothFragment())
        }
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Solo su Android 12+
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )

            val missingPermissions = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_CODE_BLUETOOTH)
            }
        }
    }

    // Gestione della risposta dell'utente ai permessi richiesti
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("Bluetooth", "Permessi Bluetooth concessi")
            } else {
                Log.e("Bluetooth", "Permessi Bluetooth negati")
            }
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}

