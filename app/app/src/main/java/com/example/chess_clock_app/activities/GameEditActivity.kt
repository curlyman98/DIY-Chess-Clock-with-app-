package com.example.chess_clock_app.activities

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chess_clock_app.R
import com.example.chess_clock_app.chess.PieceType
import com.example.chess_clock_app.viewmodels.GameEditUiState
import com.example.chess_clock_app.viewmodels.GameEditViewModel
import com.example.chess_clock_app.views.ChessBoardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class GameEditActivity : BaseActivity() {

    companion object {
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

        val gameKey = intent.getIntExtra(EXTRA_GAME, -1)
        if (gameKey == -1) {
            Toast.makeText(this, R.string.game_missing, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        viewModel.loadGame(gameKey)

        val titleText = findViewById<TextView>(R.id.gameTitle)
        val importDateText = findViewById<TextView>(R.id.gameImportDate)
        val board = findViewById<ChessBoardView>(R.id.chessBoard)
        val movesText = findViewById<TextView>(R.id.movesText)
        val previousButton = findViewById<MaterialButton>(R.id.previousMoveButton)
        val saveButton = findViewById<MaterialButton>(R.id.saveMovesButton)
        val nextButton = findViewById<MaterialButton>(R.id.nextMoveButton)

        onBackPressedDispatcher.addCallback(this, unsavedChangesCallback)

        // The app draws behind the system navigation bar, so keep the buttons above it
        val buttonsRow = findViewById<LinearLayout>(R.id.moveButtonsRow)
        ViewCompat.setOnApplyWindowInsetsListener(buttonsRow) { view, insets ->
            view.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }

        board.onMoveRequested = { from, to, isPromotion ->
            if (isPromotion) {
                askPromotionPiece { piece -> viewModel.makeMove(from, to, piece) }
            } else {
                viewModel.makeMove(from, to)
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
                    importDateText.text = formatImportTime(state.importTime)
                    board.position = state.position
                    board.lastMove = state.lastMove
                    movesText.text = formatMoves(state)

                    previousButton.setEnabledLook(state.canGoBack)
                    nextButton.setEnabledLook(state.canGoForward)
                    saveButton.setEnabledLook(state.hasUnsavedChanges)
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

    // "2026-10-01T18:30" -> "Imported 1 Oct 2026, 18:30"; anything else is shown as it is
    private fun formatImportTime(raw: String): String {
        if (raw.isBlank()) return ""
        val formatted = try {
            LocalDateTime.parse(raw).format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm"))
        } catch (e: DateTimeParseException) {
            raw
        }
        return getString(R.string.imported_on, formatted)
    }

    // One line per move pair: "1. e4 (4:58)   e5 (4:51)".
    // Clock times without a move yet show as "…". The move on the board is highlighted.
    private fun formatMoves(state: GameEditUiState): CharSequence {
        val halfMoves = maxOf(state.sans.size, state.clocks.size)
        if (halfMoves == 0) return getString(R.string.no_moves)

        val text = SpannableStringBuilder()
        for (ply in 0 until halfMoves) {
            if (ply % 2 == 0) text.append("${ply / 2 + 1}.".padEnd(4))

            val start = text.length
            text.append(state.sans.getOrNull(ply) ?: "…")
            state.clocks.getOrNull(ply)?.let { text.append(" (${it.removePrefix("0:")})") }
            if (ply == state.currentPly - 1) {
                text.setSpan(StyleSpan(Typeface.BOLD), start, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.setSpan(BackgroundColorSpan(Color.argb(90, 255, 235, 59)), start, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            if (ply % 2 == 0) text.append("   ") else text.append("\n")
        }
        return text
    }

    private fun MaterialButton.setEnabledLook(enabled: Boolean) {
        isEnabled = enabled
        alpha = if (enabled) 1f else 0.4f
    }
}
