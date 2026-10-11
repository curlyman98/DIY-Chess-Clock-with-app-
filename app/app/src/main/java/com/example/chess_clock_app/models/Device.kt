package com.example.chess_clock_app.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")

data class Device(
    @PrimaryKey
    val mac: String,
    val name: String,

    // val macAddress: String
    //  Last connection
)