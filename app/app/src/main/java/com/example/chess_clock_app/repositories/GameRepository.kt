package com.example.chess_clock_app.repositories

import com.example.chess_clock_app.dao.GameDao
import com.example.chess_clock_app.models.Game
import kotlinx.coroutines.flow.Flow

// Single place the app gets game data from.
// Later syncing games from the ESP32 can live here too.
class GameRepository(private val gameDao: GameDao) {

    val gamesByCreatedAt: Flow<List<Game>> = gameDao.getGameOrderedByCreatedAt()

    val gamesByName: Flow<List<Game>> = gameDao.getGameOrderedByName()

    suspend fun getGame(key: Int): Game? = gameDao.getById(key)

    // Returns the key of the new game
    suspend fun addGame(game: Game): Long = gameDao.insertGame(game)

    suspend fun updateGame(game: Game) = gameDao.update(game)

    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)
}
