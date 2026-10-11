package com.example.chess_clock_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chess_clock_app.ChessClockApplication
import com.example.chess_clock_app.models.Game
import com.example.chess_clock_app.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameEditViewModel(private val repository: GameRepository) : ViewModel() {

    // Private mutable version for the ViewModel, read-only version for the activity
    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    fun loadGame(key: Int) {
        // Already loaded (e.g. activity recreated after rotation): don't hit the DB again
        if (_game.value?.key == key) return
        viewModelScope.launch { _game.value = repository.getGame(key) }
    }

    fun saveGame(name: String, pgn: String) {
        val current = _game.value ?: return
        val updated = current.copy(name = name, pgn = pgn)
        _game.value = updated
        viewModelScope.launch { repository.updateGame(updated) }
    }

    fun deleteGame() {
        val current = _game.value ?: return
        viewModelScope.launch { repository.deleteGame(current) }
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
