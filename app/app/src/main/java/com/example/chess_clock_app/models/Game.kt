package com.example.chess_clock_app.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")

data class Game(
    val pgn: String = "",
    val name: String = "",
    // MAC of the chess clock the game was imported from; empty for games added in the app
    val espMac: String = "",
    // When the game was created or imported, in milliseconds since 1970 (System.currentTimeMillis())
    val createdAt: Long = 0L,
    @PrimaryKey(autoGenerate = true)
    val key: Int = 0
)
