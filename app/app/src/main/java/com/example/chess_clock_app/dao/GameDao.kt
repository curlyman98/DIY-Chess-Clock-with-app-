package com.example.chess_clock_app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chess_clock_app.models.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert
    suspend fun insertGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    @Update
    suspend fun update(game: Game)

    @Query("SELECT * FROM games ORDER BY importTime ASC")
    fun getGameOrderedByImportTime(): Flow<List<Game>>

    @Query("SELECT * FROM games ORDER BY name ASC")
    fun getGameOrderedByName(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE key = :key LIMIT 1")
    suspend fun getById(key: Int): Game?

}

