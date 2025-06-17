package com.ali.demono.features.game.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val id: Int = 1, // Only one active session
    val currentRound: Int = 1,
    val gameType: String = "MASRI",
    val timestamp: Long = System.currentTimeMillis()
) 