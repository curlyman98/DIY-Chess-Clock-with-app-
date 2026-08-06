package com.example.chess_clock_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class DeviceAdapter(
    private val devices: List<Device>
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {

        val deviceName: TextView =
            view.findViewById(R.id.deviceName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_device,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount() =
        devices.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.deviceName.text =
            devices[position].name
    }
}
