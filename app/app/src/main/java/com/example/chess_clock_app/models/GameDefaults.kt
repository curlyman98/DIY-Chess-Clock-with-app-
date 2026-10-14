package com.example.chess_clock_app.models

import com.example.chess_clock_app.chess.Pgn
import com.example.chess_clock_app.chess.PgnGame
import com.example.chess_clock_app.utils.formatDateTime

// What a game looks like before the user changes anything. Used for games created with
// "Add game" and, later, for games synced from the ESP32.
//
// These values are stored in the database and the PGN, so they live in code instead of
// strings.xml: a saved game must not depend on the language the phone is set to.
object GameDefaults {

    const val UNKNOWN_PLAYER = "Unknown GM"

    fun name(createdAt: Long) = "Game Created on ${formatDateTime(createdAt)}"

    // Not saved yet: key = 0 until it is inserted into the database
    fun newGame(createdAt: Long = System.currentTimeMillis()): Game {
        val name = name(createdAt)
        val pgn = withDefaultTags(PgnGame(), name, createdAt)
        return Game(name = name, createdAt = createdAt, pgn = Pgn.write(pgn))
    }

    // Adds any of the standard PGN header tags that are missing; existing values are kept
    fun withDefaultTags(pgn: PgnGame, name: String, createdAt: Long): PgnGame {
        val defaults = linkedMapOf(
            "Event" to name,
            "Site" to "?",
            "Date" to Pgn.formatDate(createdAt),
            "Round" to "?",
            "White" to UNKNOWN_PLAYER,
            "Black" to UNKNOWN_PLAYER,
            "Result" to pgn.result,
        )
        // TODO: Site, Date and Round can't be edited in the app yet
        return pgn.copy(tags = defaults + pgn.tags)
    }
}
