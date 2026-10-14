package com.example.chess_clock_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chess_clock_app.ChessClockApplication
import com.example.chess_clock_app.models.Game
import com.example.chess_clock_app.repositories.DeviceRepository
import com.example.chess_clock_app.repositories.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GameSortOrder { CREATED_AT, NAME }

class GameListViewModel(
    private val repository: GameRepository,
    deviceRepository: DeviceRepository,
) : ViewModel() {

    // Sync only works while a chess clock is connected
    val isConnected: StateFlow<Boolean> = deviceRepository.isConnected

    private val sortOrder = MutableStateFlow(GameSortOrder.CREATED_AT)

    // Switches to the matching query whenever the sort order changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val games: StateFlow<List<Game>> = sortOrder
        .flatMapLatest { order ->
            when (order) {
                GameSortOrder.CREATED_AT -> repository.gamesByCreatedAt
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
                GameListViewModel(app.gameRepository, app.deviceRepository)
            }
        }
    }
}
