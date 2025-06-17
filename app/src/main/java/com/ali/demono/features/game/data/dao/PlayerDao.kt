package com.ali.demono.features.game.data.dao

import androidx.room.*
import com.ali.demono.features.game.data.model.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    
    @Query("SELECT * FROM players WHERE roundNumber = :roundNumber")
    fun getPlayersByRound(roundNumber: Int): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<PlayerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    @Update
    suspend fun updatePlayer(player: PlayerEntity)
    
    @Query("UPDATE players SET currentScore = currentScore + :scoreToAdd WHERE id = :playerId")
    suspend fun addScoreToPlayer(playerId: String, scoreToAdd: Int)
    
    @Query("UPDATE players SET currentScore = 0 WHERE roundNumber = :roundNumber")
    suspend fun resetScoresForRound(roundNumber: Int)
    
    @Query("UPDATE players SET currentScore = 0")
    suspend fun resetAllScores()
    
    @Query("UPDATE players SET roundNumber = :roundNumber")
    suspend fun updateAllPlayersToRound(roundNumber: Int)
    
    @Query("UPDATE players SET roundNumber = :newRoundNumber WHERE roundNumber = :oldRoundNumber")
    suspend fun updateRoundNumber(oldRoundNumber: Int, newRoundNumber: Int)
    
    @Delete
    suspend fun deletePlayer(player: PlayerEntity)
    
    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
} 