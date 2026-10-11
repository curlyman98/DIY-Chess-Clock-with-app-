package com.example.chess_clock_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chess_clock_app.ChessClockApplication
import com.example.chess_clock_app.models.Game
import com.example.chess_clock_app.repositories.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GameSortOrder { IMPORT_TIME, NAME }

class GameListViewModel(private val repository: GameRepository) : ViewModel() {

    private val sortOrder = MutableStateFlow(GameSortOrder.IMPORT_TIME)

    // Switches to the matching query whenever the sort order changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val games: StateFlow<List<Game>> = sortOrder
        .flatMapLatest { order ->
            when (order) {
                GameSortOrder.IMPORT_TIME -> repository.gamesByImportTime
                GameSortOrder.NAME -> repository.gamesByName
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: GameSortOrder) {
        sortOrder.value = order
    }

    fun addGame(game: Game) {
        viewModelScope.launch { repository.addGame(game) }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch { repository.deleteGame(game) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ChessClockApplication
                GameListViewModel(app.gameRepository)
            }
        }
    }
}
