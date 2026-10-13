package com.example.chess_clock_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chess_clock_app.ChessClockApplication
import com.example.chess_clock_app.chess.Move
import com.example.chess_clock_app.chess.Pgn
import com.example.chess_clock_app.chess.PgnGame
import com.example.chess_clock_app.chess.PieceType
import com.example.chess_clock_app.chess.Position
import com.example.chess_clock_app.models.Game
import com.example.chess_clock_app.repositories.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Everything the screen needs to draw itself, in one object
data class GameEditUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val name: String = "",
    val importTime: String = "",
    val position: Position = Position.start(),
    val lastMove: Move? = null,
    // How many half-moves into the game the board currently is (0 = start position)
    val currentPly: Int = 0,
    val sans: List<String> = emptyList(),
    val clocks: List<String> = emptyList(),
    val hasUnsavedChanges: Boolean = false,
) {
    val canGoBack: Boolean get() = currentPly > 0
    val canGoForward: Boolean get() = currentPly < sans.size
}

class GameEditViewModel(private val repository: GameRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GameEditUiState())
    val uiState: StateFlow<GameEditUiState> = _uiState.asStateFlow()

    private var game: Game? = null
    private var pgn = PgnGame()

    // positions[i] is the board after i half-moves, so positions[0] is the start position
    private val positions = mutableListOf(Position.start())
    private val moves = mutableListOf<Move>()
    private val sans = mutableListOf<String>()
    private var currentPly = 0

    // The moves as they are in the database; differing from `sans` means unsaved changes
    private var savedSans = emptyList<String>()

    fun loadGame(key: Int) {
        // Already loaded (e.g. activity recreated after rotation): don't hit the DB again
        if (game?.key == key) return
        viewModelScope.launch {
            val loaded = repository.getGame(key)
            if (loaded == null) {
                _uiState.value = GameEditUiState(isLoading = false, notFound = true)
                return@launch
            }
            game = loaded
            pgn = Pgn.parse(loaded.pgn)
            replay(pgn.sans)
            savedSans = sans.toList()
            publish()
        }
    }

    // Returns false if the move is not legal in the position currently shown
    fun makeMove(from: Int, to: Int, promotion: PieceType? = null): Boolean {
        val position = positions[currentPly]
        val move = position.legalMoves()
            .firstOrNull { it.from == from && it.to == to && it.promotion == promotion }
            ?: return false

        if (currentPly < moves.size) {
            // Same move as the one already recorded: just step forward, keep the rest
            if (moves[currentPly] == move) {
                currentPly++
                publish()
                return true
            }
            // A different move from an earlier position replaces everything after it
            moves.subList(currentPly, moves.size).clear()
            sans.subList(currentPly, sans.size).clear()
            positions.subList(currentPly + 1, positions.size).clear()
        }

        sans += position.toSan(move)
        moves += move
        positions += position.play(move)
        currentPly++
        publish()
        return true
    }

    fun previousMove() {
        if (currentPly > 0) {
            currentPly--
            publish()
        }
    }

    fun nextMove() {
        if (currentPly < moves.size) {
            currentPly++
            publish()
        }
    }

    // Returns the database write, so the caller can wait for it (e.g. before leaving the screen)
    fun save(): Job {
        val current = game ?: return Job().apply { complete() }
        pgn = pgn.copy(sans = sans.toList())
        val updated = current.copy(pgn = Pgn.write(pgn))
        game = updated
        savedSans = sans.toList()
        publish()
        return viewModelScope.launch { repository.updateGame(updated) }
    }

    fun discardChanges() {
        replay(savedSans)
        publish()
    }

    fun deleteGame() {
        val current = game ?: return
        viewModelScope.launch { repository.deleteGame(current) }
    }

    // Rebuilds the move list from SAN strings, starting at the start position
    private fun replay(sansToPlay: List<String>) {
        positions.subList(1, positions.size).clear()
        moves.clear()
        sans.clear()
        currentPly = 0
        for (san in sansToPlay) {
            val position = positions.last()
            // Stop at the first move that isn't legal; everything after it can't be shown either
            val move = position.moveFromSan(san) ?: break
            sans += position.toSan(move)
            moves += move
            positions += position.play(move)
        }
    }

    private fun publish() {
        val current = game ?: return
        _uiState.value = GameEditUiState(
            isLoading = false,
            name = current.name,
            importTime = current.importTime,
            position = positions[currentPly],
            lastMove = moves.getOrNull(currentPly - 1),
            currentPly = currentPly,
            sans = sans.toList(),
            clocks = pgn.clocks,
            hasUnsavedChanges = sans != savedSans,
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ChessClockApplication
                GameEditViewModel(app.gameRepository)
            }
        }
    }
}
