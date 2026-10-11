package com.example.chess_clock_app

import android.app.Application
import androidx.room.Room
import com.example.chess_clock_app.databases.ChessClockDatabase
import com.example.chess_clock_app.repositories.DeviceRepository
import com.example.chess_clock_app.repositories.GameRepository

// shared by the whole app.
class ChessClockApplication : Application() {

    // `by lazy`: built on first use, only once, thread-safe
    val database by lazy {
        Room.databaseBuilder(this, ChessClockDatabase::class.java, "chess_clock_db").build()
    }

    val deviceRepository by lazy { DeviceRepository(database.deviceDao()) }
    val gameRepository by lazy { GameRepository(database.gameDao()) }
}
