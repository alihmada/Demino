package com.ali.demono.features.game.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ali.demono.features.game.data.model.PlayerScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerScoreDao {

    @Query("SELECT * FROM player_scores WHERE roundNumber = :roundNumber")
    fun getScoresForRound(roundNumber: Int): Flow<List<PlayerScoreEntity>>

    @Query("SELECT * FROM player_scores WHERE playerId = :playerId ORDER BY roundNumber DESC")
    fun getPlayerScoreHistory(playerId: String): Flow<List<PlayerScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerScore(playerScore: PlayerScoreEntity)

    @Query("SELECT SUM(score) FROM player_scores WHERE playerId = :playerId")
    suspend fun getTotalScoreForPlayer(playerId: String): Int?

    @Query("DELETE FROM player_scores WHERE roundNumber = :roundNumber")
    suspend fun deleteScoresForRound(roundNumber: Int)
} 