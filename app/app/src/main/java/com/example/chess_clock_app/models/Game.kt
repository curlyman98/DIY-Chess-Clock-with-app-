package com.example.chess_clock_app.models
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "games")
data class Game(
    val pgn: String = "",
    val name: String = "",
    val espID: String = "",
    val importTime: String = "",
    @PrimaryKey(autoGenerate = true)
    val key: Int = 0
)