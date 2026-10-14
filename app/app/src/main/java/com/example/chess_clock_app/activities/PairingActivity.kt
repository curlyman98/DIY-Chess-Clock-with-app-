package com.example.chess_clock_app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.example.chess_clock_app.viewmodels.PairingViewModel
import kotlinx.coroutines.launch

class PairingActivity : BaseActivity() {

    private val viewModel: PairingViewModel by viewModels { PairingViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert pairing-specific UI into activity_base.xml.
        setActivityContent(R.layout.content_pairing)

        val recyclerView =
            findViewById<RecyclerView>(R.id.devicesRecyclerView)

        val noPairedDevicesText =
            findViewById<TextView>(R.id.noPairedDevicesText)

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        // Re-runs every time the devices table changes; only while the screen is visible
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedDevices.collect { devices ->
                    showDevices(recyclerView, devices)
                    noPairedDevicesText.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
        val pairButton =
            findViewById<Button>(R.id.pairButton)

        val nearbyRecyclerView =
            findViewById<RecyclerView>(R.id.nearbyDevicesRecyclerView)
        val nearbyDevicesTitle = findViewById<TextView>(R.id.nearbyDevicesTitle)

        val scanningIndicator =
            findViewById<ProgressBar>(R.id.scanningIndicator)
        val nearbyDevices = listOf(
            Device("CC",name = "Living Room Clock"),
            Device("BB", name= "Tournament Clock"),
            Device("AA", name="ESP32 Test")
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

            onDeviceSettingsClicked = {
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

    private fun showDevices(recyclerView: RecyclerView, devices: List<Device>) {
        recyclerView.adapter = DeviceAdapter(
            devices = devices,

            onDeviceClicked = { device ->
                Toast.makeText(
                    this,
                    "Connecting to ${device.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },

            // Opens the device screen
            onDeviceSettingsClicked = { device ->
                val intent = Intent(this, DeviceActivity::class.java)

                intent.putExtra(DeviceActivity.EXTRA_DEVICE, device.mac)

                startActivity(intent)
            }
        )
    }
}
