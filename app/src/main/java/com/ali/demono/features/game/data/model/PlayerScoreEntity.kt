package com.ali.demono.features.game.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "player_scores",
    primaryKeys = ["playerId", "roundNumber"],
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlayerScoreEntity(
    val playerId: String,
    val roundNumber: Int,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
) 