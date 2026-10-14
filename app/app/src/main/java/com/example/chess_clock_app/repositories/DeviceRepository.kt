package com.example.chess_clock_app.repositories

import com.example.chess_clock_app.dao.DeviceDao
import com.example.chess_clock_app.models.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Single place the app gets device data from.
// Later the ESP32 scanning/pairing logic can live here too.
class DeviceRepository(private val deviceDao: DeviceDao) {

    val savedDevices: Flow<List<Device>> = deviceDao.getAllDevices()


    // The chess clock the app is talking to; null = none connected
    // TODO: set this from the ESP32 connection code once it exists
    private val _connectedDevice = MutableStateFlow<Device?>(null)
    val connectedDevice: StateFlow<Device?> = _connectedDevice.asStateFlow()

    suspend fun getDevice(mac: String): Device? = deviceDao.getByMac(mac)

    // Emits again whenever the device changes (e.g. renamed); null once it is forgotten
    fun observeDevice(mac: String): Flow<Device?> = deviceDao.observeByMac(mac)

    suspend fun saveDevice(device: Device) = deviceDao.insertDevice(device)

    suspend fun updateDevice(device: Device) = deviceDao.updateDevice(device)

    suspend fun forgetDevice(device: Device) = deviceDao.deleteDevice(device)
}
