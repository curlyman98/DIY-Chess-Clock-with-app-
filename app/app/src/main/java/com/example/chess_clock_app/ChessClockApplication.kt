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

            //TODO Remove before final version, this formats the db upon structure change
            .fallbackToDestructiveMigration(dropAllTables = true)

            .build()
    }
    val deviceRepository by lazy { DeviceRepository(database.deviceDao()) }
    val gameRepository by lazy { GameRepository(database.gameDao()) }

    // Runs when the database is first created on the device, and again when it is wiped
    // because the version changed. To re-seed otherwise, uninstall the app or clear its storage.
    private val seedTestData = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            insertTestData(db)
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            insertTestData(db)
        }

        private fun insertTestData(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT INTO games (name, pgn, espID, createdAt) VALUES
                    ('Grenke R9', '{[%clk 0:04:58]} {[%clk 0:04:55]} {[%clk 0:04:51]} {[%clk 0:04:47]} {[%clk 0:04:40]} {[%clk 0:04:31]}', 'ESP-01', 1790872200000),
                    ('Random with Kriss', '[Event "Random with Kriss"]
                [Site "?"]
                [Date "2026.10.05"]
                [Round "?"]
                [White "Kriss"]
                [Black "Unknown GM"]
                [Result "*"]

                1. d4 {[%clk 0:09:57]} d5 {[%clk 0:09:52]} 2. c4 {[%clk 0:09:41]} e6 {[%clk 0:09:30]} {[%clk 0:09:12]} {[%clk 0:09:01]} *', 'ESP-01', 1791224100000),
                    ('Bib session', '1. c4 e5 2. Nc3 Nf6', 'ESP-02', 1791633600000)
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
