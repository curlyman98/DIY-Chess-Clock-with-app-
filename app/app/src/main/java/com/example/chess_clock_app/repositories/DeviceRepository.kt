package com.example.chess_clock_app.repositories

import com.example.chess_clock_app.dao.DeviceDao
import com.example.chess_clock_app.models.Device
import kotlinx.coroutines.flow.Flow

// Single place the app gets device data from.
// Later the ESP32 scanning/pairing logic can live here too.
class DeviceRepository(private val deviceDao: DeviceDao) {

    val savedDevices: Flow<List<Device>> = deviceDao.getAllDevices()

    suspend fun getDevice(mac: String): Device? = deviceDao.getByMac(mac)

    suspend fun saveDevice(device: Device) = deviceDao.insertDevice(device)

    suspend fun updateDevice(device: Device) = deviceDao.updateDevice(device)

    suspend fun forgetDevice(device: Device) = deviceDao.deleteDevice(device)
}
