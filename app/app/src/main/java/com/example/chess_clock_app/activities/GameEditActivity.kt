package com.example.chess_clock_app.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_clock_app.R
import com.example.chess_clock_app.adapters.MoveAdapter
import com.example.chess_clock_app.chess.PieceType
import com.example.chess_clock_app.chess.Side
import com.example.chess_clock_app.utils.formatDateTime
import com.example.chess_clock_app.utils.setEnabledLook
import com.example.chess_clock_app.viewmodels.GameEditViewModel
import com.example.chess_clock_app.views.ChessBoardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class GameEditActivity : BaseActivity() {

    companion object {
        // Key of the game to open. Started without it, the screen creates a new game.
        const val EXTRA_GAME = "com.example.chess_clock_app.extra.GAME"
    }

    private val viewModel: GameEditViewModel by viewModels { GameEditViewModel.Factory }

    // Only takes over the back button while there are unsaved changes
    private val unsavedChangesCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
                return
            }
            confirmLeave { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.content_game_edit)

        if (intent.hasExtra(EXTRA_GAME)) {
            viewModel.loadGame(intent.getIntExtra(EXTRA_GAME, -1))
        } else {
            viewModel.startNewGame()
        }

        val titleText = findViewById<TextView>(R.id.gameTitle)
        val dateText = findViewById<TextView>(R.id.gameDate)
        val board = findViewById<ChessBoardView>(R.id.chessBoard)
        val whitePlayer = findViewById<TextView>(R.id.whitePlayer)
        val blackPlayer = findViewById<TextView>(R.id.blackPlayer)
        val movesRecyclerView = findViewById<RecyclerView>(R.id.movesRecyclerView)
        val noMovesText = findViewById<TextView>(R.id.noMovesText)
        val previousButton = findViewById<Button>(R.id.previousMoveButton)
        val saveButton = findViewById<Button>(R.id.saveMovesButton)
        val nextButton = findViewById<Button>(R.id.nextMoveButton)

        onBackPressedDispatcher.addCallback(this, unsavedChangesCallback)

        val moveAdapter = MoveAdapter()
        movesRecyclerView.layoutManager = LinearLayoutManager(this)
        movesRecyclerView.adapter = moveAdapter

        board.onMoveRequested = { from, to, isPromotion ->
            if (isPromotion) {
                askPromotionPiece { piece -> viewModel.makeMove(from, to, piece) }
            } else {
                viewModel.makeMove(from, to)
            }
        }

        // Tapping the title or a player name opens a popup to edit it
        titleText.setOnClickListener {
            showEditDialog(R.string.rename_game, viewModel.uiState.value.name) { newName ->
                viewModel.rename(newName)
            }
        }
        whitePlayer.setOnClickListener {
            showEditDialog(R.string.edit_player_name, viewModel.uiState.value.white) { newName ->
                viewModel.setPlayerName(Side.WHITE, newName)
            }
        }
        blackPlayer.setOnClickListener {
            showEditDialog(R.string.edit_player_name, viewModel.uiState.value.black) { newName ->
                viewModel.setPlayerName(Side.BLACK, newName)
            }
        }

        previousButton.setOnClickListener { viewModel.previousMove() }
        nextButton.setOnClickListener { viewModel.nextMove() }
        saveButton.setOnClickListener {
            viewModel.save()
            Toast.makeText(this, R.string.moves_saved, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.notFound) {
                        Toast.makeText(this@GameEditActivity, R.string.game_not_found,
                            Toast.LENGTH_LONG).show()
                        finish()
                        return@collect
                    }
                    if (state.isLoading) return@collect

                    titleText.text = state.name
                    dateText.text = formatDateTime(state.createdAt)
                    whitePlayer.text = state.white
                    blackPlayer.text = state.black
                    board.position = state.position
                    board.lastMove = state.lastMove

                    moveAdapter.updateRows(state.moveRows)
                    noMovesText.visibility = if (state.moveRows.isEmpty()) View.VISIBLE else View.GONE
                    // Only scrolls if the current move is out of view
                    if (state.moveRows.isNotEmpty()) movesRecyclerView.scrollToPosition(state.currentRow)

                    previousButton.setEnabledLook(state.canGoBack)
                    nextButton.setEnabledLook(state.canGoForward)
                    saveButton.setEnabledLook(state.canSave)
                    unsavedChangesCallback.isEnabled = state.hasUnsavedChanges
                }
            }
        }
    }

    // Called by the back button (above) and by the drawer before it opens another screen
    override fun confirmLeave(proceed: () -> Unit) {
        if (!viewModel.uiState.value.hasUnsavedChanges) {
            proceed()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.unsaved_changes_title)
            .setMessage(R.string.unsaved_changes_message)
            .setPositiveButton(R.string.save) { _, _ ->
                // Wait until the game is written before leaving
                lifecycleScope.launch {
                    viewModel.save().join()
                    proceed()
                }
            }
            .setNegativeButton(R.string.discard) { _, _ ->
                viewModel.discardChanges()
                proceed()
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    // Popup with a text field, filled with the current value
    private fun showEditDialog(@StringRes title: Int, currentValue: String, onDone: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(R.id.editText)
        editText.setText(currentValue)
        editText.selectAll()

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(R.string.ok) { _, _ -> onDone(editText.text.toString()) }
            .setNegativeButton(R.string.cancel, null)
            .create()

        // Open the keyboard right away
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        editText.requestFocus()
        dialog.show()
    }

    private fun askPromotionPiece(onChosen: (PieceType) -> Unit) {
        val pieces = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
        val names = arrayOf(
            getString(R.string.queen),
            getString(R.string.rook),
            getString(R.string.bishop),
            getString(R.string.knight),
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.promote_to)
            .setItems(names) { _, which -> onChosen(pieces[which]) }
            .show()
    }
}
