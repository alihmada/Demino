package com.ali.demono.features.game.data.dao

import androidx.room.*
import com.ali.demono.features.game.data.model.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    
    @Query("SELECT * FROM game_sessions WHERE id = 1")
    fun getCurrentSession(): Flow<GameSessionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)
    
    @Update
    suspend fun updateSession(session: GameSessionEntity)
    
    @Query("UPDATE game_sessions SET currentRound = currentRound + 1 WHERE id = 1")
    suspend fun incrementRound()
    
    @Query("UPDATE game_sessions SET gameType = :gameType WHERE id = 1")
    suspend fun updateGameType(gameType: String)
} 