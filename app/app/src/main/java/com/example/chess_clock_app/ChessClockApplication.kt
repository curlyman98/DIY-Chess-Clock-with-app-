package com.example.chess_clock_app

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chess_clock_app.databases.ChessClockDatabase
import com.example.chess_clock_app.repositories.DeviceRepository
import com.example.chess_clock_app.repositories.GameRepository

// shared by the whole app.
class ChessClockApplication : Application() {

    // `by lazy`: built on first use, only once, thread-safe
    val database by lazy {
        Room.databaseBuilder(this, ChessClockDatabase::class.java, "chess_clock_db")
            .addCallback(seedTestData) // TODO: remove once games come from the ESP32
            .build()
    }
    val deviceRepository by lazy { DeviceRepository(database.deviceDao()) }
    val gameRepository by lazy { GameRepository(database.gameDao()) }

    // onCreate runs only once: when the database file is first created on the device.
    // To re-seed, uninstall the app or clear its storage.
    private val seedTestData = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT INTO games (name, pgn, espID, importTime) VALUES
                    ('Grenke R9', '1. e4 e5 2. Nf3 Nc6 3. Bb5 a6', 'ESP-01', '2026-10-01T18:30'),
                    ('Random with Kriss', '1. d4 d5 2. c4 e6', 'ESP-01', '2026-10-05T20:15'),
                    ('Bib session', '1. c4 e5 2. Nc3 Nf6', 'ESP-02', '2026-10-10T14:00')
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO devices (mac, name) VALUES
                    ('AA:AA:AA:AA:AA:01', 'Living Room Clock'),
                    ('AA:AA:AA:AA:AA:02', 'Tournament Clock')
                """.trimIndent()
            )
        }
    }
}
