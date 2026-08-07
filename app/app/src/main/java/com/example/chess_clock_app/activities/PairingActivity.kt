package com.example.chess_clock_app.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_clock_app.R
import com.example.chess_clock_app.adapters.DeviceAdapter
import com.example.chess_clock_app.models.Device
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

class PairingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert pairing-specific UI into activity_base.xml.
        setActivityContent(R.layout.content_pairing)

        val recyclerView =
            findViewById<RecyclerView>(R.id.devicesRecyclerView)

        val devices = listOf(
            Device("Living Room Clock"),
            Device("Tournament Clock"),
            Device("ESP32 Test")
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = DeviceAdapter(
            devices = devices,

            onDeviceClicked = { device ->
                Toast.makeText(
                    this,
                    "Connecting to ${device.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },

            onSettingsClicked = { device ->
                Toast.makeText(
                    this,
                    "Settings for ${device.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        val pairButton =
            findViewById<Button>(R.id.pairButton)

        val nearbyRecyclerView =
            findViewById<RecyclerView>(R.id.nearbyDevicesRecyclerView)
        val nearbyDevicesTitle = findViewById<TextView>(R.id.nearbyDevicesTitle)

        val scanningIndicator =
            findViewById<ProgressBar>(R.id.scanningIndicator)
        val nearbyDevices = listOf(
                    Device("ESP32 Chess Clock"),
                    Device("Workshop ESP32"),
                    Device("Unknown Device")
        )


        nearbyRecyclerView.layoutManager =
            LinearLayoutManager(this)

        nearbyRecyclerView.adapter = DeviceAdapter(
            devices = nearbyDevices,

            onDeviceClicked = { device ->
                Toast.makeText(
                    this,
                    "Pairing with ${device.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },

            onSettingsClicked = {
                // Nearby devices do not need settings yet.
            }
        )
        var isSearching = false
        pairButton.setOnClickListener {

            isSearching = !isSearching

            if (isSearching) {
                scanningIndicator.visibility = View.VISIBLE
                nearbyRecyclerView.visibility = View.VISIBLE
                nearbyDevicesTitle.visibility = View.VISIBLE
                pairButton.text = getString(R.string.stop_search)
            } else {
                scanningIndicator.visibility = View.GONE
                nearbyRecyclerView.visibility = View.GONE
                nearbyDevicesTitle.visibility = View.GONE
                pairButton.text = getString(R.string.pair_new_device)
            }
        }

    }
}