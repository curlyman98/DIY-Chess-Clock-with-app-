package com.example.chess_clock_app.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_clock_app.R
import com.example.chess_clock_app.adapters.GameAdapter
import com.example.chess_clock_app.models.Game
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.content.Intent
//import android.widget.TextView

class GameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert pairing-specific UI into activity_base.xml.
        setActivityContent(R.layout.content_games)

        val recyclerView =
            findViewById<RecyclerView>(R.id.gamesRecyclerView)

        val games = listOf(
            Game("Grenke R9"),
            Game("Random with Kriss"),
            Game("Bib session")
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = GameAdapter(
            games = games,

            onGameClicked = { games ->
                Toast.makeText(
                    this,
                    "Opening game ${games.name}",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, GameEditActivity::class.java)
                // TODO fix by adding the package to gradle
                intent.putExtra("GAME", games)

                startActivity(intent)

            },
            onGameSettingsClicked = { games ->
                Toast.makeText(
                    this,
                    "Settings for game ${games.name}",
                    Toast.LENGTH_SHORT
                ).show()

            }

        )
        val syncButton =
            findViewById<Button>(R.id.syncButton)

        val scanningIndicator =
            findViewById<ProgressBar>(R.id.syncingIndicator)

        var isSyncing = false
        syncButton.setOnClickListener {

            isSyncing = !isSyncing

            if (isSyncing) {
                scanningIndicator.visibility = View.VISIBLE

                syncButton.text = getString(R.string.cancel_sync)
            } else {
                scanningIndicator.visibility = View.GONE
                syncButton.text = getString(R.string.sync)
            }
        }

    }
}