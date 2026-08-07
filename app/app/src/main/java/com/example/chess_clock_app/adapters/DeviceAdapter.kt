package com.example.chess_clock_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_clock_app.R
import com.example.chess_clock_app.models.Device

class DeviceAdapter(
    private val devices: List<Device>,
    private val onDeviceClicked: (Device) -> Unit,
    private val onSettingsClicked: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val deviceName: TextView =
            view.findViewById(R.id.deviceName)

        val settingsButton: ImageButton =
            view.findViewById(R.id.settingsButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_device, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val device = devices[position]

        holder.deviceName.text = device.name

        holder.itemView.setOnClickListener {
            onDeviceClicked(device)
        }

        holder.settingsButton.setOnClickListener {
            onSettingsClicked(device)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}