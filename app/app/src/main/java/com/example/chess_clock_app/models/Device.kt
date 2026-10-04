package com.example.chess_clock_app.models
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "devices")
data class Device(
    val name: String,

    // val macAddress: String
    //  Last connection

    val key: PrimaryKey

)