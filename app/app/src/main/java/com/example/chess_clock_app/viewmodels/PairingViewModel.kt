package com.example.chess_clock_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chess_clock_app.ChessClockApplication
import com.example.chess_clock_app.models.Device
import com.example.chess_clock_app.repositories.DeviceRepository
import com.example.chess_clock_app.utils.InputLimits
import com.example.chess_clock_app.utils.cleanInput
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PairingViewModel(private val repository: DeviceRepository) : ViewModel() {

    // Remembers the latest list, so after a rotation the UI gets it instantly
    val savedDevices: StateFlow<List<Device>> = repository.savedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDevice(device: Device) {
        viewModelScope.launch { repository.saveDevice(device) }
    }

    // An empty name keeps the old one
    fun renameDevice(device: Device, newName: String) {
        val name = newName.cleanInput(InputLimits.DEVICE_NAME).ifEmpty { device.name }
        viewModelScope.launch { repository.updateDevice(device.copy(name = name)) }
    }

    fun forgetDevice(device: Device) {
        viewModelScope.launch { repository.forgetDevice(device) }
    }

    companion object {
        // Android creates ViewModels itself; this tells it how to pass in the repository
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ChessClockApplication
                PairingViewModel(app.deviceRepository)
            }
        }
    }
}
