package com.example.chess_clock_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PairingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)

        val recyclerView =
            findViewById<RecyclerView>(
                R.id.devicesRecyclerView
            )
        val devices = listOf(
                    Device("Living Room Clock"),
                    Device("Tournament Clock"),
                    Device("ESP32 Test")
        )
        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter =
            DeviceAdapter(devices)
    }
}