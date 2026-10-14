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
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chess_clock_app.utils.setEnabledLook
import com.example.chess_clock_app.viewmodels.GameListViewModel
import kotlinx.coroutines.launch
//import android.widget.TextView

class GameActivity : BaseActivity() {

    // Same instance survives rotation; created through the Factory the first time
    private val viewModel: GameListViewModel by viewModels { GameListViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert pairing-specific UI into activity_base.xml.
        setActivityContent(R.layout.content_games)

        val recyclerView =
            findViewById<RecyclerView>(R.id.gamesRecyclerView)

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        // Re-runs every time the games table changes; only while the screen is visible
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.games.collect { games ->
                    showGames(recyclerView, games)
                }
            }
        }

        val syncButton =
            findViewById<Button>(R.id.syncButton)

        val scanningIndicator =
            findViewById<ProgressBar>(R.id.syncingIndicator)

        // Sync is greyed out while no chess clock is connected
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isConnected.collect { connected ->
                    syncButton.setEnabledLook(connected)
                }
            }
        }

        // Opens the edit screen without a game key, which means "new game"
        val addGameButton =
            findViewById<Button>(R.id.addGameButton)

        addGameButton.setOnClickListener {
            startActivity(Intent(this, GameEditActivity::class.java))
        }

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

    private fun showGames(recyclerView: RecyclerView, games: List<Game>) {
        recyclerView.adapter = GameAdapter(
            games = games,

            onGameClicked = { games ->
                Toast.makeText(
                    this,
                    getString(R.string.opening_game, games.name),
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, GameEditActivity::class.java)

                intent.putExtra(GameEditActivity.EXTRA_GAME, games.key)

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
    }
}
