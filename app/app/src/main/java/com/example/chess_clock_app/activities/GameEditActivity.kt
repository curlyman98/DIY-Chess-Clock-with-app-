package com.example.chess_clock_app.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chess_clock_app.R
import com.example.chess_clock_app.adapters.GameAdapter
import com.example.chess_clock_app.models.Game
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.IntentCompat
import androidx.appcompat.app.AppCompatActivity

class GameEditActivity : AppCompatActivity() {

        companion object {
            const val EXTRA_GAME = "com.example.chess_clock_app.extra.GAME"
        }

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_game_edit)

            val game = IntentCompat.getParcelableExtra(
                        intent,
                        EXTRA_GAME,
                        Game::class.java
            )
            if (game == null) {
                Toast.makeText(
                    this,
                    "Unable to open game: game data is missing.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }


            println(game.name)
            val gameNameField = findViewById<TextView>(R.id.titleText)
            val gamePGN = findViewById<TextView>(R.id.gamePGN)
            val gameID = findViewById<TextView>(R.id.gameID)

            gameNameField.text = game.name
            gamePGN.text = game.pgn
            gameID.text = game.iD
        }


    }
