package com.example.lab6

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanner: BluetoothLeScanner
    private lateinit var listView: ListView
    private val deviceList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        scanner = bluetoothAdapter.bluetoothLeScanner

        listView = findViewById(R.id.deviceListView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = adapter

        findViewById<Button>(R.id.scanButton).setOnClickListener {
            checkPermissionsAndStartScan()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionsAndStartScan() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            startScan()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startScan() {
        deviceList.clear()
        adapter.notifyDataSetChanged()

        val scanCallback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device.name ?: "Tuntematon laite"
                val address = result.device.address
                val rssi = result.rssi

                val info = "$name\n$address\nRSSI: $rssi dBm"

                if (!deviceList.contains(info)) {
                    runOnUiThread {
                        deviceList.add(info)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        scanner.startScan(scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(scanCallback)
            Toast.makeText(this, "Skannaus lopetettu", Toast.LENGTH_SHORT).show()
        }, 3000)
    }
}